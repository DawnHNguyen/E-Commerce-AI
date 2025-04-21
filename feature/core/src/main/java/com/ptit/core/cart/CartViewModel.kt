package com.ptit.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.utils.Resource
import com.ptit.presentation.viewmodel.ProductDetailState
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

    init {
        getPurchases()
    }

    fun getPurchases(status: Int? = null) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading

            when (val result = purchaseRepository.getPurchases(status)) {
                is Resource.Success -> _cartState.value = CartState.Success(result.data)
                is Resource.Error -> _cartState.value = CartState.Error(result.error.message?: "Unknown error")
                is Resource.Loading -> _cartState.value = CartState.Loading
                Resource.Idle -> TODO()
                else -> _cartState.value = CartState.Error("Unexpected error")
            }
        }
    }

    sealed class CartState {
        object Loading : CartState()
        data class Success(val purchases: List<PurchaseDomainEntity>) : CartState()
        data class Error(val message: String) : CartState()
    }
}