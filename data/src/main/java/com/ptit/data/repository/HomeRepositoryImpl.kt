package com.ptit.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.data.remote.pagingsource.ProductPagingSource
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(private val remoteDataSource: ProductRemoteDataSource) : HomeRepository {
    override fun paginatedRecommendedProduct(): Flow<PagingData<ProductDomainEntity>> =
        Pager(
            config = PagingConfig(
                pageSize = RECOMMENDED_PRODUCT_PAGE_SIZE,
                prefetchDistance = RECOMMENDED_PRODUCT_PAGE_SIZE / 2,
                initialLoadSize = RECOMMENDED_PRODUCT_PAGE_SIZE
            ),
            pagingSourceFactory = {
                ProductPagingSource(
                    remoteDataSource = remoteDataSource,
                )
            }
        ).flow

    override fun searchProducts(query: String): Flow<PagingData<ProductDomainEntity>> =
        Pager(
            config = PagingConfig(
                pageSize = SEARCH_PRODUCT_PAGE_SIZE,
                prefetchDistance = SEARCH_PRODUCT_PAGE_SIZE / 2,
                initialLoadSize = SEARCH_PRODUCT_PAGE_SIZE
            ),
            pagingSourceFactory = {
                ProductPagingSource(
                    remoteDataSource = remoteDataSource,
                    name = query
                )
            }
        ).flow

    override suspend fun getProducts(amount: Int): Resource<List<ProductDomainEntity>> =
        remoteDataSource.listProducts(page = 1, limit = amount, sortBy = "createdAt", orderBy = "desc",
            minPrice = null, maxPrice = null, name = null, categories = null, brandIds = null
        ).map { listProductResponse ->
            listProductResponse.data?.map { it.toDomainEntity() } ?: emptyList()   }

    companion object {
        private const val RECOMMENDED_PRODUCT_PAGE_SIZE = 20
        private const val SEARCH_PRODUCT_PAGE_SIZE = 20
    }
}