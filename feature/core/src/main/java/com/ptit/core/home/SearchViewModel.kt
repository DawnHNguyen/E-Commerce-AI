package com.ptit.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _categoriesState = MutableStateFlow<Resource<List<CategoryDomainEntity>>>(Resource.idle())
    val categoriesState = _categoriesState.asStateFlow()

    private val _trendingProductsState = MutableStateFlow<Resource<List<ProductDomainEntity>>>(Resource.idle())
    val trendingProductsState = _trendingProductsState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<ProductDomainEntity>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            homeRepository.searchProducts(query)
        }
        .cachedIn(viewModelScope)

    init {
        loadCategories()
        loadTrendingProducts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = Resource.loading()
            _categoriesState.value = productRepository.getCategories()
        }
    }

    private fun loadTrendingProducts() {
        viewModelScope.launch {
            _trendingProductsState.value = Resource.loading()
            _trendingProductsState.value = homeRepository.getTrendingProducts()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}