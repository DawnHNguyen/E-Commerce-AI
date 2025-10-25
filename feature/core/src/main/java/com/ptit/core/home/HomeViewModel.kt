package com.ptit.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.ProductRepository // Thêm ProductRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val products: List<ProductDomainEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = productRepository.listProducts(
                page = 1,
                limit = 20,
                sortBy = "createdAt",
                orderBy = "desc"
            )) {
                is Resource.Success -> {
                    _uiState.update { it.copy( isLoading = false, products = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy( isLoading = false, error = result.error.message) }
                }

                else -> {
                    // Bỏ qua các trường hợp khác
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun refreshData() {
        fetchProducts()
    }
}