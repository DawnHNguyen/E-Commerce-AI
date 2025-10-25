package com.ptit.domain.repository

import androidx.paging.PagingData
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun paginatedRecommendedProduct(): Flow<PagingData<ProductDomainEntity>>
    fun searchProducts(query: String): Flow<PagingData<ProductDomainEntity>>
    suspend fun getProducts(amount: Int): Resource<List<ProductDomainEntity>>
}