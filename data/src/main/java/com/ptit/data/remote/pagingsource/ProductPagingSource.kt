package com.ptit.data.remote.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.utils.Resource

class ProductPagingSource(
    private val remoteDataSource: ProductRemoteDataSource,
    private val name: String? = null,
    private val rating: Int? = null,
    private val minPrice: Int? = null,
    private val maxPrice: Int? = null,
    private val sortBy: String? = null,
): PagingSource<Int, ProductDomainEntity>() {
    override fun getRefreshKey(state: PagingState<Int, ProductDomainEntity>): Int? = null


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductDomainEntity> {
        val start = params.key ?: 1

        val response = remoteDataSource.listProducts(
            page = start,
            limit = params.loadSize,
            name = name,
            rating = rating,
            minPrice = minPrice,
            maxPrice = maxPrice,
            sortBy = sortBy,
        )

        return if (response is Resource.Success) {
            val data = response.data.toDomainEntity()
            val nextKey = if (data.products.size < params.loadSize) null else start + 1
            LoadResult.Page(
                data = data.products,
                prevKey = if (start == 1) null else start - 1,
                nextKey = nextKey,
            )
        } else {
            LoadResult.Error(Throwable((response as? Resource.Error)?.error?.message ?: "Unknown error occurred"))
        }
    }
}