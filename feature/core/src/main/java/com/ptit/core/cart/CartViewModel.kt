package com.ptit.core.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.entity.cart.CartItemDomainEntity
import com.ptit.domain.entity.cart.DeleteCartRequestDomainEntity
import com.ptit.domain.repository.CartRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow<CartState>(CartState.Loading)
    val cartState: StateFlow<CartState> = _cartState

    private val _updateCartState =
        MutableStateFlow<UpdateCartState>(UpdateCartState.Idle)
    val updateCartState: StateFlow<UpdateCartState> = _updateCartState

    private val _deleteCartState =
        MutableStateFlow<DeleteCartState>(DeleteCartState.Idle)
    val deleteCartState: StateFlow<DeleteCartState> = _deleteCartState

    init {
        getCart()
    }

    /**
     * 🛒 Lấy danh sách sản phẩm trong giỏ hàng
     */
    fun getCart(page: Int? = 1, limit: Int? = 20) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading

            when (val result = cartRepository.getCart(page, limit)) {
                is Resource.Success -> _cartState.value =
                    CartState.Success(result.data.data)
                is Resource.Error -> _cartState.value =
                    CartState.Error(result.error.message ?: "Unknown error")
                else -> _cartState.value =
                    CartState.Error("Unexpected error")
            }
        }
    }

    /**
     * 🧮 Cập nhật số lượng 1 sản phẩm trong giỏ hàng
     */
    fun updateCartItemQuantity(cartItemId: String, skuId: String, quantity: Int) {
        viewModelScope.launch {
            _updateCartState.value = UpdateCartState.Loading

            when (val result = cartRepository.updateCartItem(cartItemId, skuId, quantity)) {
                is Resource.Success -> {
                    _updateCartState.value = UpdateCartState.Success
                    getCart()
                }

                is Resource.Error -> _updateCartState.value =
                    UpdateCartState.Error(result.error.message ?: "Failed to update quantity")

                else -> _updateCartState.value =
                    UpdateCartState.Error("Unexpected error")
            }
        }
    }

    /**
     * 🗑️ Xóa 1 hoặc nhiều sản phẩm trong giỏ
     */
    fun deleteCartItems(cartItemIds: List<String>) {
        viewModelScope.launch {
            _deleteCartState.value = DeleteCartState.Loading

            // ✅ Tạo request body đúng key với backend: "cartItemIds"
            val request = DeleteCartRequestDomainEntity(cartItemIds = cartItemIds)

            // ✅ Gọi Repository để thực thi API
            when (val result = cartRepository.deleteCartItems(request)) {
                is Resource.Success -> {
                    val deletedCount = result.data.deletedCount
                    _deleteCartState.value = DeleteCartState.Success(deletedCount)
                    // ✅ Làm mới lại giỏ hàng để cập nhật UI
                    getCart()
                }

                is Resource.Error -> {
                    _deleteCartState.value = DeleteCartState.Error(
                        result.error.message ?: "Failed to delete items"
                    )
                }

                else -> _deleteCartState.value =
                    DeleteCartState.Error("Unexpected error")
            }
        }
    }
    fun getFlatCartItems(groupedItems: List<CartItemDetailDomainEntity>): List<CartItemDomainEntity> {
        return groupedItems.flatMap { it.cartItems }
    }

    // Giữ nguyên cấu trúc state cũ để UI không phải thay đổi
    sealed class CartState {
        object Loading : CartState()
        data class Success(val groupedItems: List<CartItemDetailDomainEntity>) : CartState()
        data class Error(val message: String) : CartState()
    }

    sealed class UpdateCartState {
        object Idle : UpdateCartState()
        object Loading : UpdateCartState()
        object Success : UpdateCartState()
        data class Error(val message: String) : UpdateCartState()
    }

    sealed class DeleteCartState {
        object Idle : DeleteCartState()
        object Loading : DeleteCartState()
        data class Success(val deletedCount: Int) : DeleteCartState()
        data class Error(val message: String) : DeleteCartState()
    }
}
