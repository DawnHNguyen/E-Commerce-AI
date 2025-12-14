package com.ptit.data.remote.api

import com.ptit.data.remote.dto.review.CreateReviewRequestDto
import com.ptit.data.remote.dto.review.CreateReviewResponseDto
import com.ptit.data.remote.dto.review.GetReviewsResponseDto
import com.ptit.data.remote.dto.review.UpdateReviewRequestDto
import com.ptit.data.remote.dto.review.UpdateReviewResponseDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApi {

    @GET("reviews/products/{productId}")
    suspend fun getReviews(
        @Path("productId") productId: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): Resource<GetReviewsResponseDto>

    @GET("reviews/check")
    suspend fun checkReviewExists(
        @Query("orderId") orderId: String,
        @Query("productId") productId: String
    ): Resource<Boolean>

    @POST("reviews")
    suspend fun createReview(
        @Body body: CreateReviewRequestDto
    ): Resource<CreateReviewResponseDto>

    @PUT("reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: String,
        @Body body: UpdateReviewRequestDto
    ): Resource<UpdateReviewResponseDto>
}

