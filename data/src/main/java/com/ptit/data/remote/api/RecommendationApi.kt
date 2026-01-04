package com.ptit.data.remote.api

import com.ptit.data.remote.dto.recommendation.GetRecommendationsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecommendationApi {
    @GET("recommend/{userId}")
    suspend fun getRecommendations(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 20
    ): GetRecommendationsResponse

    @GET("recommend/product/{productId}")
    suspend fun getProductRecommendations(
        @Path("productId") productId: String,
        @Query("limit") limit: Int = 10
    ): GetRecommendationsResponse
}

