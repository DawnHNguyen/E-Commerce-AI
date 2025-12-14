package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ReviewApi
import com.ptit.data.remote.dto.review.CreateReviewRequestDto
 import com.ptit.data.remote.dto.review.GetReviewsResponseDto
import com.ptit.data.remote.dto.review.UpdateReviewRequestDto
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class ReviewRemoteDataSource @Inject constructor(
    private val remoteService: ReviewApi
) {
    suspend fun getReviews(
        productId: String,
        page: Int,
        limit: Int
    ): Resource<GetReviewsResponseDto> = remoteService.getReviews(productId, page, limit)

    suspend fun checkReviewExists(
        orderId: String,
        productId: String
    ) = remoteService.checkReviewExists(orderId, productId)

    suspend fun createReview(
        body: CreateReviewRequestDto
    ) = remoteService.createReview(body)

    suspend fun updateReview(
        reviewId: String,
        body: UpdateReviewRequestDto
    ) = remoteService.updateReview(reviewId, body)
}

