package com.ptit.core.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.chat.ChatDeliveryAddress
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.UiTag
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity
import com.ptit.domain.repository.ChatRepository
import com.ptit.domain.usecase.chat.GetChatHistoryUseCase
import com.ptit.domain.usecase.chat.SendChatMessageUseCase
import com.ptit.domain.usecase.chat.ClearChatHistoryUseCase
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationEvent {
    data object AddPayment : NavigationEvent()
    data object Checkout : NavigationEvent()
    data class OrderDetail(val orderId: String) : NavigationEvent()
}

/**
 * Address input state for checkout flow (matching CreateOrderScreen)
 */
data class AddressInputState(
    // User info (auto-filled from profile)
    val recipientName: String = "",
    val phone: String = "",

    // Address selection
    val provinces: List<ProvinceEntity> = emptyList(),
    val districts: List<DistrictEntity> = emptyList(),
    val wards: List<WardEntity> = emptyList(),
    val selectedProvince: ProvinceEntity? = null,
    val selectedDistrict: DistrictEntity? = null,
    val selectedWard: WardEntity? = null,
    val detailAddress: String = "",

    // Loading states
    val provincesLoading: Boolean = false,
    val districtsLoading: Boolean = false,
    val wardsLoading: Boolean = false,

    // Shipping fee
    val shippingFee: Int = 0,
    val isCalculatingShippingFee: Boolean = false
) {
    val isAddressComplete: Boolean
        get() = selectedProvince != null && selectedDistrict != null &&
                selectedWard != null && detailAddress.isNotBlank()

    val isFormValid: Boolean
        get() = recipientName.isNotBlank() && phone.isNotBlank() && isAddressComplete
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _addressState = MutableStateFlow(AddressInputState())
    val addressState: StateFlow<AddressInputState> = _addressState.asStateFlow()

    private var currentSessionId: String? = null
    private var historyObserverJob: Job? = null

    fun setSessionId(sessionId: String) {
        if (currentSessionId == sessionId) return

        currentSessionId = sessionId
        _uiState.update { it.copy(sessionId = sessionId) }

        // Cancel previous observer and start new one
        historyObserverJob?.cancel()
        observeChatHistory(sessionId)
    }

    private fun observeChatHistory(sessionId: String) {
        historyObserverJob = getChatHistoryUseCase(sessionId)
            .onEach { messages ->
                _uiState.update { it.copy(messages = messages) }

                // Check for navigation events in the latest AI message
                messages.lastOrNull()?.let { lastMessage ->
                    processNavigationTags(lastMessage)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processNavigationTags(message: ChatMessage) {
        message.tags.forEach { tag ->
            when (tag) {
                is UiTag.NavigateToScreen -> {
                    when (tag.screen) {
                        "add_payment" -> {
                            _uiState.update {
                                it.copy(navigationEvent = NavigationEvent.AddPayment)
                            }
                        }
                        "checkout" -> {
                            _uiState.update {
                                it.copy(navigationEvent = NavigationEvent.Checkout)
                            }
                        }
                        "order_detail" -> {
                            // Extract order ID if present in the message
                            val orderIdRegex = Regex("""order[_\s]?id[:\s]+([A-Za-z0-9-]+)""", RegexOption.IGNORE_CASE)
                            orderIdRegex.find(message.content)?.groupValues?.get(1)?.let { orderId ->
                                _uiState.update {
                                    it.copy(navigationEvent = NavigationEvent.OrderDetail(orderId))
                                }
                            }
                        }
                    }
                }
                else -> { /* Other tags are handled in UI */ }
            }
        }
    }

    fun sendMessage(message: String) {
        val sessionId = currentSessionId ?: return
        if (message.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            sendChatMessageUseCase(sessionId, message)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = resource.error.message ?: "Unknown error"
                                )
                            }
                        }
                        else -> {}
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun clearHistory() {
        val sessionId = currentSessionId ?: return

        viewModelScope.launch {
            when (val result = clearChatHistoryUseCase(sessionId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(messages = emptyList()) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.error.message ?: "Failed to clear history")
                    }
                }
                else -> {}
            }
        }
    }

    fun clearNavigationEvent() {
        _uiState.update { it.copy(navigationEvent = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ==================== ADDRESS INPUT METHODS ====================

    /**
     * Initialize address input - load provinces and user profile
     */
    fun initAddressInput() {
        loadProvinces()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Get cached name/phone from repository
        chatRepository.getUserNameAndPhone()?.let { (name, phone) ->
            _addressState.update {
                it.copy(recipientName = name, phone = phone)
            }
        }
    }

    private fun loadProvinces() {
        viewModelScope.launch {
            _addressState.update { it.copy(provincesLoading = true) }
            when (val result = chatRepository.getProvinces()) {
                is Resource.Success -> {
                    _addressState.update {
                        it.copy(provinces = result.data, provincesLoading = false)
                    }
                }
                is Resource.Error -> {
                    _addressState.update { it.copy(provincesLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectProvince(province: ProvinceEntity?) {
        if (province == null) {
            _addressState.update {
                it.copy(
                    selectedProvince = null,
                    selectedDistrict = null,
                    selectedWard = null,
                    districts = emptyList(),
                    wards = emptyList(),
                    shippingFee = 0
                )
            }
            return
        }

        if (province.id == _addressState.value.selectedProvince?.id) return

        _addressState.update {
            it.copy(
                selectedProvince = province,
                selectedDistrict = null,
                selectedWard = null,
                districts = emptyList(),
                wards = emptyList(),
                shippingFee = 0
            )
        }
        loadDistricts(province.id)
    }

    private fun loadDistricts(provinceId: Int) {
        viewModelScope.launch {
            _addressState.update { it.copy(districtsLoading = true) }
            when (val result = chatRepository.getDistricts(provinceId)) {
                is Resource.Success -> {
                    _addressState.update {
                        it.copy(districts = result.data, districtsLoading = false)
                    }
                }
                is Resource.Error -> {
                    _addressState.update { it.copy(districtsLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectDistrict(district: DistrictEntity?) {
        if (district == null) {
            _addressState.update {
                it.copy(
                    selectedDistrict = null,
                    selectedWard = null,
                    wards = emptyList(),
                    shippingFee = 0
                )
            }
            return
        }

        if (district.id == _addressState.value.selectedDistrict?.id) return

        _addressState.update {
            it.copy(
                selectedDistrict = district,
                selectedWard = null,
                wards = emptyList(),
                shippingFee = 0
            )
        }
        loadWards(district.id)
    }

    private fun loadWards(districtId: Int) {
        viewModelScope.launch {
            _addressState.update { it.copy(wardsLoading = true) }
            when (val result = chatRepository.getWards(districtId)) {
                is Resource.Success -> {
                    _addressState.update {
                        it.copy(wards = result.data, wardsLoading = false)
                    }
                }
                is Resource.Error -> {
                    _addressState.update { it.copy(wardsLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectWard(ward: WardEntity?) {
        _addressState.update { it.copy(selectedWard = ward) }
        // Calculate shipping when ward is selected and address is complete
        if (ward != null && _addressState.value.detailAddress.isNotBlank()) {
            calculateShippingFee()
        }
    }

    fun updateRecipientName(name: String) {
        _addressState.update { it.copy(recipientName = name) }
    }

    fun updatePhone(phone: String) {
        _addressState.update { it.copy(phone = phone) }
    }

    fun updateDetailAddress(address: String) {
        _addressState.update { it.copy(detailAddress = address) }
        // Calculate shipping when address is complete
        if (_addressState.value.selectedWard != null && address.isNotBlank()) {
            calculateShippingFee()
        }
    }

    private fun calculateShippingFee() {
        val state = _addressState.value
        if (!state.isAddressComplete) return

        viewModelScope.launch {
            _addressState.update { it.copy(isCalculatingShippingFee = true) }

            // First, update the delivery address in repository
            val address = ChatDeliveryAddress(
                recipientName = state.recipientName,
                phone = state.phone,
                provinceId = state.selectedProvince?.id,
                provinceName = state.selectedProvince?.name,
                districtId = state.selectedDistrict?.id,
                districtName = state.selectedDistrict?.name,
                wardCode = state.selectedWard?.code,
                wardName = state.selectedWard?.name,
                detailAddress = state.detailAddress,
                isDefault = false
            )
            chatRepository.updateDeliveryAddress(address)

            // Then calculate shipping fee
            val fee = chatRepository.calculateShippingFee()

            _addressState.update {
                it.copy(shippingFee = fee, isCalculatingShippingFee = false)
            }
        }
    }

    /**
     * Save address and trigger checkout refresh
     */
    fun saveAddressAndCheckout() {
        val state = _addressState.value
        if (!state.isFormValid) return

        val address = ChatDeliveryAddress(
            recipientName = state.recipientName,
            phone = state.phone,
            provinceId = state.selectedProvince?.id,
            provinceName = state.selectedProvince?.name,
            districtId = state.selectedDistrict?.id,
            districtName = state.selectedDistrict?.name,
            wardCode = state.selectedWard?.code,
            wardName = state.selectedWard?.name,
            detailAddress = state.detailAddress,
            isDefault = false
        )

        chatRepository.updateDeliveryAddress(address)

        // Trigger checkout flow again with new address
        sendMessage("Đặt hàng")
    }

    /**
     * Reset address state
     */
    fun resetAddressState() {
        _addressState.update { AddressInputState() }
    }
}

data class ChatUiState(
    val sessionId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigationEvent: NavigationEvent? = null
)
