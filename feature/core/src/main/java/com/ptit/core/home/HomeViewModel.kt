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
    val trendingProducts: List<ProductDomainEntity> = emptyList(),
    val recommendedProducts: List<ProductDomainEntity> = emptyList(),
    val isLoadingTrending: Boolean = false,
    val isLoadingRecommended: Boolean = false,
    val errorTrending: String? = null,
    val errorRecommended: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchTrendingProducts()
        fetchHomeRecommendations()
    }

    fun fetchTrendingProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrending = true, errorTrending = null) }
            when (val result = homeRepository.getTrendingProducts(amount = 3)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoadingTrending = false, trendingProducts = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingTrending = false, errorTrending = result.error.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoadingTrending = false) }
                }
            }
        }
    }

    fun fetchHomeRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecommended = true, errorRecommended = null) }
            when (val result = productRepository.getHomeRecommendations()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoadingRecommended = false, recommendedProducts = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingRecommended = false, errorRecommended = result.error.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoadingRecommended = false) }
                }
            }
        }
    }

    fun refreshData() {
        fetchTrendingProducts()
        fetchHomeRecommendations()
    }
}