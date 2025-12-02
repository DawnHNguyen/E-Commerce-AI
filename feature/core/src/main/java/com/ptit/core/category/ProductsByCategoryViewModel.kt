package com.ptit.core.category

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
    val categoryDisplayName: String = "",
    val products: List<ProductDomainEntity> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProductsByCategoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsByCategoryUiState())
    val uiState: StateFlow<ProductsByCategoryUiState> = _uiState.asStateFlow()

    fun loadProductsByCategory(categoryId: String, categoryDisplayName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    categoryDisplayName = categoryDisplayName
                )
            }

            when (val result = productRepository.listProducts(
                page = 1,
                limit = 100,
                categories = listOf(categoryId)
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = result.data,
                            error = if (result.data.isEmpty()) "Không có sản phẩm nào trong danh mục này." else null
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.message ?: "Lỗi tải sản phẩm."
                        )
                    }
                }

                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }

                is Resource.Idle -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Không có yêu cầu nào được gửi đến."
                        )
                    }
                }
            }
        }
    }
}

