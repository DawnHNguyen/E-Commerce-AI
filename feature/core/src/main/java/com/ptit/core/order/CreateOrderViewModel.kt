package com.ptit.core.order

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderState(
    val selectedItems: List<PurchaseDomainEntity> = emptyList(),
    val name: TextFieldValue = TextFieldValue(""),
    val phone: TextFieldValue = TextFieldValue(""),
    val address: TextFieldValue = TextFieldValue(""),
    val note: TextFieldValue = TextFieldValue(""),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val purchaseRepository: PurchaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _orderState = MutableStateFlow(OrderState())
    val orderState = _orderState.asStateFlow()

    init {
        // Get selected items from the arguments
        val selectedItems = savedStateHandle.get<ArrayList<PurchaseDomainEntity>>("selectedItems")
        selectedItems?.let {
            _orderState.update { state -> state.copy(selectedItems = selectedItems) }
        }

        // Fetch user profile to pre-fill shipping information
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true) }

            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    val user = result.data
                    _orderState.update { state ->
                        state.copy(
                            name = TextFieldValue(user.name ?: ""),
                            phone = TextFieldValue(user.phone ?: ""),
                            address = TextFieldValue(user.shop.address ?: ""),
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _orderState.update { it.copy(
                        isLoading = false,
                        error = result.error.message
                    ) }
                }
                else -> _orderState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateName(value: TextFieldValue) {
        _orderState.update { it.copy(name = value) }
    }

    fun updatePhone(value: TextFieldValue) {
        _orderState.update { it.copy(phone = value) }
    }

    fun updateAddress(value: TextFieldValue) {
        _orderState.update { it.copy(address = value) }
    }

    fun updateNote(value: TextFieldValue) {
        _orderState.update { it.copy(note = value) }
    }

    fun setSelectedItems(items: List<PurchaseDomainEntity>) {
        _orderState.update { it.copy(selectedItems = items) }
    }

    fun setSelectedItemIds(selectedItemIds: List<String>) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true) }

            try {
                // Get the full purchase objects for each selected ID
                val purchases = purchaseRepository.getPurchasesByIds(selectedItemIds)
                _orderState.update { state ->
                    state.copy(
                        selectedItems = purchases,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _orderState.update { state ->
                    state.copy(
                        error = "Failed to load selected items: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    // Function to create the order - this will be implemented later
    fun createOrder() {
        // Implementation will connect to your backend API
    }
}
