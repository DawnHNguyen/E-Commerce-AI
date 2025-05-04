package com.ptit.core.order

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository // Thêm UserRepository
) : ViewModel() {

    // UI state for Order creation
    private val _orderState = MutableStateFlow(OrderFormState())
    val orderState: StateFlow<OrderFormState> = _orderState.asStateFlow()

    // Events for one-time actions like navigation
    private val _orderEvents = MutableSharedFlow<OrderEvent>()
    val orderEvents: SharedFlow<OrderEvent> = _orderEvents.asSharedFlow()

    // State để theo dõi việc tải thông tin user
    private val _userProfileState = MutableStateFlow<Resource<UserDomainEntity>>(Resource.idle())
    val userProfileState: StateFlow<Resource<UserDomainEntity>> = _userProfileState.asStateFlow()

    init {
        // Lấy thông tin user khi ViewModel được khởi tạo
        loadUserProfile()
    }

    // Function to load user profile
    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.update { Resource.loading() }

            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    _userProfileState.update { Resource.success(result.data) }

                    // Cập nhật thông tin vận chuyển từ user profile
                    result.data?.let { user ->
                        _orderState.update { state ->
                            state.copy(
                                name = TextFieldValue(user.name),
                                phone = TextFieldValue(user.phone),
                                address = TextFieldValue(user.shop.address),
                                isUserInfoLoaded = true
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _userProfileState.update { Resource.error(result.error) }
                    _orderEvents.emit(OrderEvent.ShowError("Failed to load user profile: ${result.error.message}"))
                }
                else -> {}
            }
        }
    }

    // Function to set selected item IDs and load them
    fun setSelectedItemIds(itemIds: List<String>) {
        if (itemIds.isEmpty()) {
            _orderEvents.tryEmit(OrderEvent.ShowError("No items selected"))
            return
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, error = null) }

            // Load each purchase item by ID
            val purchases = mutableListOf<PurchaseDomainEntity>()
            var hasError = false

            for (id in itemIds) {
                when (val result = purchaseRepository.getPurchaseById(id)) {
                    is Resource.Success -> {
                        result.data?.let { purchases.add(it) }
                    }
                    is Resource.Error -> {
                        hasError = true
                        _orderEvents.emit(OrderEvent.ShowError("Failed to load item: ${result.error.message}"))
                    }
                    else -> {}
                }
            }

            if (hasError && purchases.isEmpty()) {
                _orderState.update { it.copy(
                    isLoading = false,
                    error = "Failed to load items"
                )}
            } else {
                _orderState.update { it.copy(
                    isLoading = false,
                    selectedItems = purchases
                )}
            }
        }
    }

    // Form field update functions
    fun updateName(value: TextFieldValue) {
        _orderState.update { it.copy(name = value, nameEdited = true) }
    }

    fun updatePhone(value: TextFieldValue) {
        _orderState.update { it.copy(phone = value, phoneEdited = true) }
    }

    fun updateAddress(value: TextFieldValue) {
        _orderState.update { it.copy(address = value, addressEdited = true) }
    }

    fun updateNote(value: TextFieldValue) {
        _orderState.update { it.copy(note = value) }
    }

    // Toggle field editing state
    fun toggleNameEditing() {
        _orderState.update { it.copy(isNameEditing = !it.isNameEditing) }
    }

    fun togglePhoneEditing() {
        _orderState.update { it.copy(isPhoneEditing = !it.isPhoneEditing) }
    }

    fun toggleAddressEditing() {
        _orderState.update { it.copy(isAddressEditing = !it.isAddressEditing) }
    }

    // Create order function
    fun createOrder() {
        val currentState = _orderState.value

        if (currentState.selectedItems.isEmpty()) {
            viewModelScope.launch {
                _orderEvents.emit(OrderEvent.ShowError("No items selected for order"))
            }
            return
        }

        if (currentState.name.text.isBlank() ||
            currentState.phone.text.isBlank() ||
            currentState.address.text.isBlank()) {
            viewModelScope.launch {
                _orderEvents.emit(OrderEvent.ShowError("Please fill in all required fields"))
            }
            return
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true) }

            // Extract item IDs
            val purchaseIds = currentState.selectedItems.map { it.id }

            // Create order request
            when (val result = orderRepository.createOrder(
                purchaseIds = purchaseIds,
                fullName = currentState.name.text,
                phone = currentState.phone.text,
                address = currentState.address.text,
                note = currentState.note.text,
                totalAmount = currentState.selectedItems.sumOf { it.price * it.buyCount },
                shippingFee = 30000
            )) {
                is Resource.Success -> {
                    _orderState.update { it.copy(isLoading = false) }
                    result.data?.let { order ->
                        _orderEvents.emit(OrderEvent.OrderCreated(order))
                    }
                }
                is Resource.Error -> {
                    _orderState.update { it.copy(
                        isLoading = false,
                        error = result.error.message
                    )}
                    _orderEvents.emit(OrderEvent.ShowError(
                        result.error.message ?: "Failed to create order"
                    ))
                }
                else -> {}
            }
        }
    }

    // State for the order form
    data class OrderFormState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedItems: List<PurchaseDomainEntity> = emptyList(),
        val name: TextFieldValue = TextFieldValue(""),
        val phone: TextFieldValue = TextFieldValue(""),
        val address: TextFieldValue = TextFieldValue(""),
        val note: TextFieldValue = TextFieldValue(""),

        // Thêm các trạng thái để quản lý việc chỉnh sửa
        val isNameEditing: Boolean = false,
        val isPhoneEditing: Boolean = false,
        val isAddressEditing: Boolean = false,
        val nameEdited: Boolean = false,
        val phoneEdited: Boolean = false,
        val addressEdited: Boolean = false,
        val isUserInfoLoaded: Boolean = false
    )
}

// Events emitted by the ViewModel
sealed class OrderEvent {
    data class ShowError(val message: String) : OrderEvent()
    data class OrderCreated(val orderResponse: CreateOrderResponseDomainEntity) : OrderEvent()
}