package com.ptit.core.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    // Product list state
    private val _productListState = MutableStateFlow<Resource<List<ProductDomainEntity>>>(Resource.Idle)
    val productListState: StateFlow<Resource<List<ProductDomainEntity>>> = _productListState

    // State for holding the current list of products
    private val _productList = MutableStateFlow<List<ProductDomainEntity>>(emptyList())
    val productList: StateFlow<List<ProductDomainEntity>> = _productList

    // State for delete operations
    private val _deleteProductState = MutableStateFlow<DeleteProductState>(DeleteProductState.Idle)
    val deleteProductState: StateFlow<DeleteProductState> = _deleteProductState

    // State for update operations
    private val _updateProductState = MutableStateFlow<UpdateProductState>(UpdateProductState.Idle)
    val updateProductState: StateFlow<UpdateProductState> = _updateProductState

    // State for product details
    private val _productDetailState = MutableStateFlow<Resource<ProductDomainEntity>>(Resource.Idle)
    val productDetailState: StateFlow<Resource<ProductDomainEntity>> = _productDetailState

    // State for add product
    private val _addProductState = MutableStateFlow<Resource<ProductDomainEntity>>(Resource.Idle)
    val addProductState: StateFlow<Resource<ProductDomainEntity>> = _addProductState

    // Fetch list of products
    fun fetchProductList() {
        viewModelScope.launch {
            _productListState.value = Resource.loading()

            when (val result = productRepository.getProductsByShop()) {
                is Resource.Success -> {
                    _productListState.value = result
                    _productList.value = result.data
                }
                is Resource.Error -> {
                    _productListState.value = result
                }
                else -> { /* Handle other cases if needed */ }
            }
        }
    }

}

// State classes for operations
sealed class DeleteProductState {
    object Idle : DeleteProductState()
    object Loading : DeleteProductState()
    data class Success(val productId: String) : DeleteProductState()
    data class Error(val message: String) : DeleteProductState()
}

sealed class UpdateProductState {
    object Idle : UpdateProductState()
    object Loading : UpdateProductState()
    data class Success(val product: ProductDomainEntity) : UpdateProductState()
    data class Error(val message: String) : UpdateProductState()
}
