package com.ptit.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.data.remote.pagingsource.ProductPagingSource
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.HomeRepository
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

    companion object {
        private const val RECOMMENDED_PRODUCT_PAGE_SIZE = 20
    }
}