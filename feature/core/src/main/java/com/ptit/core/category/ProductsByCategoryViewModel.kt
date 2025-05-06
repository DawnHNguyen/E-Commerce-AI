package com.ptit.core.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductsByCategoryUiState(
    val isLoading: Boolean = false,
    val products: List<ProductDomainEntity> = emptyList(),
    val error: String? = null,
    val categoryDisplayName: String = ""
)

@HiltViewModel
class ProductsByCategoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsByCategoryUiState())
    val uiState: StateFlow<ProductsByCategoryUiState> = _uiState.asStateFlow()

    private val categoryIdFilter: String = savedStateHandle.get<String>("categoryId") ?: ""
    private val categoryDisplayName: String = savedStateHandle.get<String>("categoryDisplayName") ?: "Sản phẩm"


    init {
        _uiState.update { it.copy(categoryDisplayName = categoryDisplayName) }
        if (categoryIdFilter.isNotEmpty()) {
            fetchProductsByCategoryId(categoryIdFilter)
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy ID danh mục.") }
        }
    }

    // Đổi tên hàm và tham số để phản ánh việc lọc bằng ID
    fun fetchProductsByCategoryId(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Giả định productRepository.getProductsByShop() trả về tất cả sản phẩm
            // hoặc bạn có một hàm khác phù hợp hơn để lấy tất cả sản phẩm.
            when (val result = productRepository.getAllProducts()) {
                is Resource.Success -> {
                    val allProducts = result.data
                    // Lọc sản phẩm dựa trên category.id
                    val filteredProducts = allProducts.filter { product ->
                        product.category.id.equals(categoryId, ignoreCase = true)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = filteredProducts,
                            error = if (filteredProducts.isEmpty()) "Không có sản phẩm nào trong danh mục này." else null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.message ?: "Lỗi tải sản phẩm"
                        )
                    }
                }
                is Resource.Loading -> {
                    // Trạng thái Loading đã được set ở đầu hàm, không cần update lại ở đây
                    // trừ khi bạn muốn có logic cụ thể cho Resource.Loading từ repository
                }
                is Resource.Idle -> {
                    _uiState.update { it.copy(isLoading = false, error = "Trạng thái không hoạt động từ repository")}
                }
            }
        }
    }
}