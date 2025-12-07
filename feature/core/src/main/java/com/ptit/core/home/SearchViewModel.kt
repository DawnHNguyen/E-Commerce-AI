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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchFilters(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val categoryIds: List<String> = emptyList(),
    val brandIds: List<String> = emptyList(),
    val sortBy: String = "createdAt", // createdAt, price, sale
    val orderBy: String = "desc" // asc, desc
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _categoriesState = MutableStateFlow<Resource<List<CategoryDomainEntity>>>(Resource.idle())
    val categoriesState = _categoriesState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow(SearchFilters())
    val filters = _filters.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<ProductDomainEntity>> = combine(
        _searchQuery.debounce(300).distinctUntilChanged(),
        _filters
    ) { query, filters ->
        Pair(query, filters)
    }.flatMapLatest { (query, currentFilters) ->
        homeRepository.searchProducts(
            query = query,
            minPrice = currentFilters.minPrice,
            maxPrice = currentFilters.maxPrice,
            categories = currentFilters.categoryIds.ifEmpty { null },
            brandIds = currentFilters.brandIds.ifEmpty { null },
            sortBy = currentFilters.sortBy,
            orderBy = currentFilters.orderBy
        )
    }.cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = Resource.loading()
            val result = productRepository.getAllCategories()
            when (result) {
                is Resource.Success -> {
                    _categoriesState.value = Resource.success(result.data.data)
                }
                is Resource.Error -> {
                    _categoriesState.value = Resource.error(result.error)
                }
                is Resource.Loading -> {
                    _categoriesState.value = Resource.loading(result.data?.data)
                }
                is Resource.Idle -> {
                    _categoriesState.value = Resource.idle()
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun updateFilters(
        minPrice: Int? = _filters.value.minPrice,
        maxPrice: Int? = _filters.value.maxPrice,
        categoryIds: List<String> = _filters.value.categoryIds,
        brandIds: List<String> = _filters.value.brandIds,
        sortBy: String = _filters.value.sortBy,
        orderBy: String = _filters.value.orderBy
    ) {
        _filters.value = SearchFilters(
            minPrice = minPrice,
            maxPrice = maxPrice,
            categoryIds = categoryIds,
            brandIds = brandIds,
            sortBy = sortBy,
            orderBy = orderBy
        )
    }

    fun clearFilters() {
        _filters.value = SearchFilters()
    }

    fun toggleCategory(categoryId: String) {
        val currentCategories = _filters.value.categoryIds.toMutableList()
        if (currentCategories.contains(categoryId)) {
            currentCategories.remove(categoryId)
        } else {
            currentCategories.add(categoryId)
        }
        updateFilters(categoryIds = currentCategories)
    }

    fun setSortBy(sortBy: String) {
        updateFilters(sortBy = sortBy)
    }

    fun setOrderBy(orderBy: String) {
        updateFilters(orderBy = orderBy)
    }

    fun setPriceRange(minPrice: Int?, maxPrice: Int?) {
        updateFilters(minPrice = minPrice, maxPrice = maxPrice)
    }

    fun applyAllFilters(
        categoryIds: List<String>,
        minPrice: Int?,
        maxPrice: Int?,
        sortBy: String,
        orderBy: String
    ) {
        _filters.value = SearchFilters(
            minPrice = minPrice,
            maxPrice = maxPrice,
            categoryIds = categoryIds,
            brandIds = _filters.value.brandIds,
            sortBy = sortBy,
            orderBy = orderBy
        )
    }
}