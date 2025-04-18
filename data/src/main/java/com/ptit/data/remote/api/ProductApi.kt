package com.ptit.data.remote.api

import com.ptit.data.remote.dto.home.ListProductResponse
import com.ptit.domain.utils.Resource
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun listProducts(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("sortBy") sortBy: String?,
        @Query("price_min") minPrice: Int?,
        @Query("price_max") maxPrice: Int?,
        @Query("rating") rating: Int?,
        @Query("name") name: String?
    ): Resource<ListProductResponse>
}