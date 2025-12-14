package com.ptit.domain.repository

import com.ptit.domain.entity.review.CreateReviewRequestDomainEntity
import com.ptit.domain.entity.review.GetReviewsResponseDomainEntity
import com.ptit.domain.entity.review.ReviewDomainEntity
import com.ptit.domain.entity.review.UpdateReviewRequestDomainEntity
import com.ptit.domain.utils.Resource

interface ReviewRepository {
    suspend fun getReviews(
        productId: String,
        page: Int = 1,
        limit: Int = 10
    ): Resource<GetReviewsResponseDomainEntity>

    suspend fun checkReviewExists(
        orderId: String,
        productId: String
    ): Resource<Boolean>

    suspend fun createReview(
        body: CreateReviewRequestDomainEntity
    ): Resource<ReviewDomainEntity>

    suspend fun updateReview(
        reviewId: String,
        body: UpdateReviewRequestDomainEntity
    ): Resource<ReviewDomainEntity>
}

