package com.ptit.domain.repository

import androidx.paging.PagingData
import com.ptit.domain.entity.product.ProductDomainEntity
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun paginatedRecommendedProduct(): Flow<PagingData<ProductDomainEntity>>
}