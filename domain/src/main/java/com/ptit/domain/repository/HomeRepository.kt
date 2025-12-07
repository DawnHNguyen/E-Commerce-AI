package com.ptit.domain.repository

import androidx.paging.PagingData
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun paginatedRecommendedProduct(): Flow<PagingData<ProductDomainEntity>>

    fun searchProducts(
        query: String,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        categories: List<String>? = null,
        brandIds: List<String>? = null,
        sortBy: String = "createdAt",
        orderBy: String = "desc"
    ): Flow<PagingData<ProductDomainEntity>>

    suspend fun getProducts(amount: Int): Resource<List<ProductDomainEntity>>
}