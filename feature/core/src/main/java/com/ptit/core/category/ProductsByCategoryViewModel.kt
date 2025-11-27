//package com.ptit.core.category
//
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.ptit.domain.entity.product.ProductDomainEntity
//import com.ptit.domain.repository.ProductRepository
//import com.ptit.domain.utils.Resource
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//data class ProductsByCategoryUiState(
//    val isLoading: Boolean = false,
//    val categoryDisplayName: String = "",
//    val products: List<ProductDomainEntity> = emptyList(),
//    val error: String? = null
//)
//
//@HiltViewModel
//class ProductsByCategoryViewModel @Inject constructor(
//    private val productRepository: ProductRepository,
//    savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ProductsByCategoryUiState())
//    val uiState: StateFlow<ProductsByCategoryUiState> = _uiState.asStateFlow()
//
//    private val categoryIdFilter: String = savedStateHandle.get<String>("categoryId") ?: ""
//    private val categoryDisplayName: String = savedStateHandle.get<String>("categoryDisplayName") ?: ""
//
//    init {
//        _uiState.update { it.copy(categoryDisplayName = categoryDisplayName) }
//
//        if (categoryIdFilter.isNotEmpty()) {
//            fetchProductsByCategoryId(categoryIdFilter)
//        } else {
//            _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy ID danh mục.") }
//        }
//    }
//
//    private fun fetchProductsByCategoryId(categoryId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _uiState.update { it.copy(isLoading = true, error = null) }
//            when (val result = productRepository.getProductsByCategory(categoryId)) {
//                is Resource.Success -> {
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            products = result.data,
//                            error = if (result.data.isEmpty()) "Không có sản phẩm nào trong danh mục này." else null
//                        )
//                    }
//                }
//
//                is Resource.Error -> {
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            error = result.error.message ?: "Lỗi tải sản phẩm."
//                        )
//                    }
//                }
//
//                is Resource.Loading -> {
//
//                }
//
//                is Resource.Idle -> {
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            error = "Không có yêu cầu nào gửi đến."
//                        )
//                    }
//                }
//            }
//        }
//    }
//}