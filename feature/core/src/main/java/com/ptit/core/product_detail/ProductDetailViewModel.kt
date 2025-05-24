package com.ptit.presentation.viewmodel // Giữ package name gốc của file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _productDetailState = MutableStateFlow<ProductDetailState>(ProductDetailState.Initial)
    val productDetailState: StateFlow<ProductDetailState> = _productDetailState.asStateFlow()

    private val _addToCartState = MutableStateFlow<AddToCartState>(AddToCartState.Initial)
    val addToCartState: StateFlow<AddToCartState> = _addToCartState.asStateFlow()

    // Thêm StateFlow cho sản phẩm gợi ý
    private val _similarProductsState = MutableStateFlow<SimilarProductsState>(SimilarProductsState.Initial)
    val similarProductsState: StateFlow<SimilarProductsState> = _similarProductsState.asStateFlow()

    fun getProductDetail(productId: String) {
        viewModelScope.launch {
            _productDetailState.value = ProductDetailState.Loading
            _similarProductsState.value = SimilarProductsState.Loading // Bắt đầu tải sản phẩm tương tự

            when (val result = productRepository.getProductDetail(productId)) {
                is Resource.Success -> {
                    _productDetailState.value = ProductDetailState.Success(result.data)
                    // Tải sản phẩm gợi ý sau khi chi tiết sản phẩm được tải thành công
                    fetchSimilarProducts(productId)
                }
                is Resource.Error -> {
                    _productDetailState.value = ProductDetailState.Error(result.error.message ?: "Unknown error")
                    _similarProductsState.value = SimilarProductsState.Error(result.error.message ?: "Unknown error fetching similar products")
                }
                else -> {
                    _productDetailState.value = ProductDetailState.Error("Unexpected error")
                    _similarProductsState.value = SimilarProductsState.Error("Unexpected error fetching similar products")
                }
            }
        }
    }

    // Hàm để tải sản phẩm gợi ý
    private fun fetchSimilarProducts(productId: String) {
        viewModelScope.launch {
            when (val result = productRepository.getSimilarProducts(productId, 6)) {
                is Resource.Success -> {
                    _similarProductsState.value = SimilarProductsState.Success(result.data)
                }
                is Resource.Error -> {
                    _similarProductsState.value = SimilarProductsState.Error(result.error.message ?: "Unknown error")
                }
                else -> {
                    _similarProductsState.value = SimilarProductsState.Error("Unexpected error")
                }
            }
        }
    }

    fun addToCart(productId: String, buyCount: Int = 1) {
        viewModelScope.launch {
            _addToCartState.value = AddToCartState.Loading

            when (val result = purchaseRepository.addToCart(productId, buyCount)) {
                is Resource.Success -> {
                    _addToCartState.value = AddToCartState.Success(result.data)
                }
                is Resource.Error -> {
                    _addToCartState.value = AddToCartState.Error(result.error.message ?: "Unknown error")
                }
                else -> {
                    _addToCartState.value = AddToCartState.Error("Unexpected error")
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

sealed class AddToCartState {
    object Initial : AddToCartState()
    object Loading : AddToCartState()
    data class Success(val purchase: PurchaseDomainEntity) : AddToCartState()
    data class Error(val message: String) : AddToCartState()
}

sealed class SimilarProductsState {
    object Initial : SimilarProductsState()
    object Loading : SimilarProductsState()
    data class Success(val products: List<ProductDomainEntity>) : SimilarProductsState()
    data class Error(val message: String) : SimilarProductsState()
}