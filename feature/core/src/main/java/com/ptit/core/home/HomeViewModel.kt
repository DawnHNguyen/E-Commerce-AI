package com.ptit.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    homeRepository: HomeRepository,
) : ViewModel() {
    val paginatedRecommendedProduct: Flow<PagingData<ProductDomainEntity>> =
        homeRepository.paginatedRecommendedProduct()
            .cachedIn(viewModelScope)
}