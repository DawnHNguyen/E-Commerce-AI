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
    private val minPrice: Int? = null,
    private val maxPrice: Int? = null,
    private val sortBy: String? = null,
    private val orderBy: String? = null,
    private val categories: List<String>? = null,
    private val brandIds: List<String>? = null
): PagingSource<Int, ProductDomainEntity>() {
    override fun getRefreshKey(state: PagingState<Int, ProductDomainEntity>): Int? = null


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductDomainEntity> {
        val page = params.key ?: 1

        val response = remoteDataSource.listProducts(
            page = page,
            limit = params.loadSize,
            sortBy = sortBy,
            orderBy = orderBy,
            minPrice = minPrice,
            maxPrice = maxPrice,
            name = name,
            categories = categories,
            brandIds = brandIds,
        )

        return if (response is Resource.Success) {
            val listProductResponse = response.data
            val products = listProductResponse.data?.map { it.toDomainEntity() } ?: emptyList()  // ✅ .data
            val metadata = listProductResponse.metadata

            LoadResult.Page(
                data = products,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (metadata?.hasNext == true) page + 1 else null,  // ✅ Dùng metadata
            )
        } else {
            LoadResult.Error(Throwable((response as? Resource.Error)?.error?.message ?: "Unknown error occurred"))
        }
    }
}