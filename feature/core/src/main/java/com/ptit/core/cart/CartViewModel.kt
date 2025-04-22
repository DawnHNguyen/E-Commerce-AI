package com.ptit.core.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow<CartState>(CartState.Loading)
    val cartState: StateFlow<CartState> = _cartState

    // Add state for update operation
    private val _updatePurchaseState = MutableStateFlow<UpdatePurchaseState>(UpdatePurchaseState.Idle)
    val updatePurchaseState: StateFlow<UpdatePurchaseState> = _updatePurchaseState

    // Add state for delete operation
    private val _deletePurchaseState = MutableStateFlow<DeletePurchaseState>(DeletePurchaseState.Idle)
    val deletePurchaseState: StateFlow<DeletePurchaseState> = _deletePurchaseState

    init {
        getPurchases()
    }

    fun getPurchases(status: Int? = -1) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading

            when (val result = purchaseRepository.getPurchases(status)) {
                is Resource.Success -> _cartState.value = CartState.Success(result.data)
                is Resource.Error -> _cartState.value = CartState.Error(result.error.message?: "Unknown error")
                is Resource.Loading -> _cartState.value = CartState.Loading
                Resource.Idle -> {}
                else -> _cartState.value = CartState.Error("Unexpected error")
            }
        }
    }

    fun updatePurchaseQuantity(productId: String, buyCount: Int) {
        viewModelScope.launch {
            _updatePurchaseState.value = UpdatePurchaseState.Loading
            // Cập nhật UI ngay lập tức
            updateLocalPurchase(productId, buyCount)

            when (val result = purchaseRepository.updatePurchase(productId, buyCount)) {
                is Resource.Success -> {
                    _updatePurchaseState.value = UpdatePurchaseState.Success(result.data)
                    // Refresh cart data after successful update
                    //getPurchases()
                }
                is Resource.Error -> {
                    _updatePurchaseState.value = UpdatePurchaseState.Error(
                        result.error.message ?: "Failed to update quantity"
                    )
                }
                is Resource.Loading -> _updatePurchaseState.value = UpdatePurchaseState.Loading
                Resource.Idle -> {}
                else -> _updatePurchaseState.value = UpdatePurchaseState.Error("Unexpected error")
            }
        }
    }
    private fun updateLocalPurchase(productId: String, buyCount: Int) {
        val currentState = _cartState.value
        if (currentState is CartState.Success) {
            val updatedPurchases = currentState.purchases.map { purchase ->
                if (purchase.product.id == productId) {
                    purchase.copy(buyCount = buyCount)
                } else {
                    purchase
                }
            }
            _cartState.value = CartState.Success(updatedPurchases)
        }
    }

    // Function to delete a single purchase
    fun deletePurchase(purchaseId: String) {
        deletePurchases(listOf(purchaseId))
    }

    // Function to delete multiple purchases
    fun deletePurchases(purchaseIds: List<String>) {
        viewModelScope.launch {
            _deletePurchaseState.value = DeletePurchaseState.Loading

            when (val result = purchaseRepository.deletePurchases(purchaseIds)) {
                is Resource.Success -> {
                    _deletePurchaseState.value = DeletePurchaseState.Success(result.data.deletedCount)
                    // Refresh cart data after successful deletion
                    getPurchases()
                }
                is Resource.Error -> {
                    _deletePurchaseState.value = DeletePurchaseState.Error(
                        result.error.message ?: "Failed to delete items"
                    )
                }
                is Resource.Loading -> _deletePurchaseState.value = DeletePurchaseState.Loading
                Resource.Idle -> {}
                else -> _deletePurchaseState.value = DeletePurchaseState.Error("Unexpected error")
            }
        }
    }

    sealed class CartState {
        object Loading : CartState()
        data class Success(val purchases: List<PurchaseDomainEntity>) : CartState()
        data class Error(val message: String) : CartState()
    }

    sealed class UpdatePurchaseState {
        object Idle : UpdatePurchaseState()
        object Loading : UpdatePurchaseState()
        data class Success(val updatedPurchase: PurchaseDomainEntity) : UpdatePurchaseState()
        data class Error(val message: String) : UpdatePurchaseState()
    }

    sealed class DeletePurchaseState {
        object Idle : DeletePurchaseState()
        object Loading : DeletePurchaseState()
        data class Success(val deletedCount: Int) : DeletePurchaseState()
        data class Error(val message: String) : DeletePurchaseState()
    }
}