package com.ptit.core.product

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

    private val _categoryListState = MutableStateFlow<Resource<List<CategoryDomainEntity>>>(Resource.Idle)
    val categoryListState: StateFlow<Resource<List<CategoryDomainEntity>>> = _categoryListState

    // Existing product list states
    private val _productListState = MutableStateFlow<Resource<List<ProductDomainEntity>>>(Resource.Idle)
    val productListState: StateFlow<Resource<List<ProductDomainEntity>>> = _productListState

    private val _productList = MutableStateFlow<List<ProductDomainEntity>>(emptyList())
    val productList: StateFlow<List<ProductDomainEntity>> = _productList

    // Product details state for fetching a specific product
    private val _productDetailsState = MutableStateFlow<Resource<ProductDomainEntity?>>(Resource.Idle)
    val productDetailsState: StateFlow<Resource<ProductDomainEntity?>> = _productDetailsState

    // State for save operations (create or update)
    private val _saveProductState = MutableStateFlow<Resource<ProductDomainEntity>>(Resource.Idle)
    val saveProductState: StateFlow<Resource<ProductDomainEntity>> = _saveProductState


    private val _uploadImagesState = MutableStateFlow<Resource<List<String>>>(Resource.Idle)
    val uploadImagesState: StateFlow<Resource<List<String>>> = _uploadImagesState

    private val _deleteProductState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val deleteProductState: StateFlow<Resource<Unit>> = _deleteProductState

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

    fun getCategories() {
        viewModelScope.launch {
            _categoryListState.value = Resource.loading()

            when (val result = productRepository.getCategories()) {
                is Resource.Success -> {
                    Log.d("ProductViewModel", "getCategories: ${result.data}")
                    _categoryListState.value = result

                }
                is Resource.Error -> {
                    _categoryListState.value = result
                }
                else -> { /* Handle other cases if needed */ }
            }
        }
    }

    // Get a specific product's details by ID
    fun getProductDetails(productId: String) {
        viewModelScope.launch {
            _productDetailsState.value = Resource.loading()

            when (val result = productRepository.getProductDetail(productId)) {
                is Resource.Success -> {
                    _productDetailsState.value = result
                }
                is Resource.Error -> {
                    _productDetailsState.value = result
                }
                else -> { /* Handle other cases if needed */ }
            }
        }
    }

    // Create a new product
    fun createProduct(
        name: String,
        description: String,
        price: Int,
        priceBeforeDiscount: Int,
        quantity: Int,
        imageFiles: List<String>,
        category: String = "",
    ) {
        viewModelScope.launch {
            Log.d("ProductViewModel", "Creating product with name: $name, category: $category")
            Log.d("ProductViewModel", "Images: ${imageFiles.joinToString()}")

            _saveProductState.value = Resource.loading()

            try {
                Log.d("ProductViewModel", "Calling productRepository.createProduct")
                val result = productRepository.createProduct(
                    name = name,
                    description = description,
                    price = price,
                    priceBeforeDiscount = priceBeforeDiscount,
                    quantity = quantity,
                    images = imageFiles,
                    image = imageFiles.firstOrNull() ?: "",
                    category = category
                )

                Log.d("ProductViewModel", "createProduct result: $result")

                when (result) {
                    is Resource.Success -> {
                        Log.d("ProductViewModel", "Product created successfully: ${result.data}")
                        _saveProductState.value = result
                        // Refresh product list
                        fetchProductList()
                    }
                    is Resource.Error -> {
                        Log.e("ProductViewModel", "Error creating product: ${result}")
                        _saveProductState.value = result
                    }
                    else -> {
                        Log.d("ProductViewModel", "Other result state: $result")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Exception in createProduct", e)
            }
        }
    }
    // Update an existing product
    fun updateProduct(
        productId: String,
        name: String,
        description: String,
        price: Int,
        priceBeforeDiscount: Int,
        quantity: Int,
        imageFiles: List<String>,
        keepImages: List<String> = emptyList(),
        category: String = "",
    ) {
        viewModelScope.launch {
            _saveProductState.value = Resource.loading()

            when (val result = productRepository.updateProduct(
                productId = productId,
                name = name,
                description = description,
                price = price,
                priceBeforeDiscount = priceBeforeDiscount,
                quantity = quantity,
                images = imageFiles,
                image = imageFiles.firstOrNull() ?: "",
                category = category
            )) {
                is Resource.Success -> {
                    _saveProductState.value = result
                    // Refresh product list
                    fetchProductList()
                }
                is Resource.Error -> {
                    _saveProductState.value = result
                }
                else -> { /* Handle other cases if needed */ }
            }
        }
    }


    fun uploadProductImages(imageUris: List<Uri>) {
        if (_uploadImagesState.value is Resource.Loading) return
        _uploadImagesState.value = Resource.loading()

        viewModelScope.launch {
            val response = fileUploadRepository.uploadMultipleFiles(imageUris)
            _uploadImagesState.value = response
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            Log.d("ProductViewModel", "Deleting product with ID: $productId")
            _deleteProductState.value = Resource.loading()

            try {
                val result = productRepository.deleteProduct(productId)
                when (result) {
                    is Resource.Success -> {
                        _deleteProductState.value = Resource.Success(Unit)
                        fetchProductList()
                    }
                    is Resource.Error -> {
                        _deleteProductState.value = Resource.Success(Unit)
                        fetchProductList()
                    }
                    is Resource.Loading -> {
                        _deleteProductState.value = Resource.loading()
                    }
                    is Resource.Idle -> {
                        _deleteProductState.value = Resource.Idle
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Exception in deleteProduct", e)
            }
        }
    }
}


