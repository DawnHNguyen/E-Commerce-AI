package com.ptit.data.remote.api

import com.ptit.data.remote.dto.product.ProductDto
import com.ptit.domain.utils.Resource
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecommendApi {
    @GET("recommendations/similar/{productId}")
    suspend fun getSimilarProducts(
        @Path("productId") productId: String,
        @Query("amount") amount: Int
    ): Resource<List<ProductDto>>
}