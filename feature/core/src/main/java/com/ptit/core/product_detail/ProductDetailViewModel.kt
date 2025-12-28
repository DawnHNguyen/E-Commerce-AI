package com.ptit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.product.SKUDomainEntity
import com.ptit.domain.entity.recommendation.RecommendedProductDomainEntity
import com.ptit.domain.repository.CartRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.repository.RecommendationRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val recommendationRepository: RecommendationRepository,
) : ViewModel() {

    private val _productDetailState = MutableStateFlow<ProductDetailState>(ProductDetailState.Initial)
    val productDetailState: StateFlow<ProductDetailState> = _productDetailState.asStateFlow()

    private val _addToCartState = MutableStateFlow<AddToCartState>(AddToCartState.Initial)
    val addToCartState: StateFlow<AddToCartState> = _addToCartState.asStateFlow()

    private val _similarProductsState = MutableStateFlow<Resource<List<RecommendedProductDomainEntity>>>(Resource.Loading())
    val similarProductsState: StateFlow<Resource<List<RecommendedProductDomainEntity>>> = _similarProductsState.asStateFlow()

    // State để track variant được chọn
    private val _selectedVariants = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedVariants: StateFlow<Map<String, String>> = _selectedVariants.asStateFlow()

    fun getProductDetail(productId: String) {
        viewModelScope.launch {
            _productDetailState.value = ProductDetailState.Loading
            _similarProductsState.value = Resource.Loading()

            when (val result = productRepository.getProductDetail(productId)) {
                is Resource.Success -> {
                    _productDetailState.value = ProductDetailState.Success(result.data)
                    // Reset selected variants khi load product mới
                    _selectedVariants.value = emptyMap()
                    fetchSimilarProducts(productId)
                }
                is Resource.Error -> {
                    _productDetailState.value = ProductDetailState.Error(result.error.message ?: "Unknown error")
                    _similarProductsState.value = Resource.Error(result.error)
                }
                else -> {
                    _productDetailState.value = ProductDetailState.Error("Unexpected error")
                    _similarProductsState.value = Resource.Error(
                        UnknownException(
                            null,
                            "Unexpected error",
                            "similar_products"
                        )
                    )

                }
            }
        }
    }
    private fun fetchSimilarProducts(productId: String) {
        viewModelScope.launch {
            // Limit 10 sản phẩm tương tự
            val result = recommendationRepository.getProductRecommendations(productId, limit = 10)
            _similarProductsState.value = result
        }
    }

    // Cập nhật variant được chọn
    fun updateSelectedVariant(variantName: String, optionValue: String) {
        _selectedVariants.value = _selectedVariants.value.toMutableMap().apply {
            put(variantName, optionValue)
        }
    }

    // Lấy SKU dựa trên variants đã chọn
    fun getSelectedSKU(product: ProductDomainEntity): SKUDomainEntity? {
        val selectedValues = selectedVariants.value.values
        return product.skus.firstOrNull { sku ->
            selectedValues.contains(sku.value)
        }
    }

    fun addToCart(skuValue: String, buyCount: Int = 1) {
        viewModelScope.launch {
            _addToCartState.value = AddToCartState.Loading

            when (val result = cartRepository.addToCart(skuValue, buyCount)) {
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

    fun resetAddToCartState() {
        _addToCartState.value = AddToCartState.Initial
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
    data class Success(val purchase: Unit) : AddToCartState()
    data class Error(val message: String) : AddToCartState()
}

sealed class SimilarProductsState {
    object Initial : SimilarProductsState()
    object Loading : SimilarProductsState()
    data class Success(val products: List<ProductDomainEntity>) : SimilarProductsState()
    data class Error(val message: String) : SimilarProductsState()
}