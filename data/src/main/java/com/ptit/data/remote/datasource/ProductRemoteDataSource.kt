package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ProductApi
import javax.inject.Inject

class ProductRemoteDataSource @Inject constructor(private val remoteService: ProductApi) {
    suspend fun listProducts(
        page: Int,
        limit: Int,
        sortBy: String?,
        minPrice: Int?,
        maxPrice: Int?,
        rating: Int?,
        name: String?
    ) = remoteService.listProducts(
        page = page,
        limit = limit,
        sortBy = sortBy,
        minPrice = minPrice,
        maxPrice = maxPrice,
        rating = rating,
        name = name
    )

    suspend fun getProductDetail(productId: String) = remoteService.getProductDetail(productId)
    suspend fun getProductsByShop() = remoteService.getProductsByShop()

    suspend fun getSimilarProducts(productId: String, amount: Int = 6) =
        remoteService.getSimilarProducts(productId, amount)
}
