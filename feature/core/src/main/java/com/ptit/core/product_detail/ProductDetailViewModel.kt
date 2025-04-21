package com.ptit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _productDetailState = MutableStateFlow<ProductDetailState>(ProductDetailState.Initial)
    val productDetailState: StateFlow<ProductDetailState> = _productDetailState.asStateFlow()

    fun getProductDetail(productId: String) {
        viewModelScope.launch {
            _productDetailState.value = ProductDetailState.Loading

            when (val result = productRepository.getProductDetail(productId)) {
                is Resource.Success -> {
                    _productDetailState.value = ProductDetailState.Success(result.data)
                }
                is Resource.Error -> {
                    _productDetailState.value = ProductDetailState.Error(result.error.message ?: "Unknown error")
                }
                else -> {
                    _productDetailState.value = ProductDetailState.Error("Unexpected error")
                }
            }
        }
    }
}

sealed class ProductDetailState {
    object Initial : ProductDetailState()
    object Loading : ProductDetailState()
    data class Success(val product: ProductDomainEntity) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}