package com.ptit.data.repository

import com.ptit.data.mapper.toDomainEntity
import com.ptit.data.mapper.toDto
import com.ptit.data.remote.datasource.ReviewRemoteDataSource
import com.ptit.data.remote.dto.review.GetReviewsResponseDto
import com.ptit.domain.entity.review.CreateReviewRequestDomainEntity
import com.ptit.domain.entity.review.GetReviewsResponseDomainEntity
import com.ptit.domain.entity.review.ReviewDomainEntity
import com.ptit.domain.entity.review.UpdateReviewRequestDomainEntity
import com.ptit.domain.repository.ReviewRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReviewRemoteDataSource
) : ReviewRepository {

    override suspend fun getReviews(
        productId: String,
        page: Int,
        limit: Int
    ): Resource<GetReviewsResponseDomainEntity> {
        val result = remoteDataSource.getReviews(productId, page, limit)
        return result.map { dto: GetReviewsResponseDto ->
            dto.toDomainEntity()
        }
    }

    override suspend fun checkReviewExists(
        orderId: String,
        productId: String
    ): Resource<Boolean> {
        return remoteDataSource.checkReviewExists(orderId, productId)
    }

    override suspend fun createReview(
        body: CreateReviewRequestDomainEntity
    ): Resource<ReviewDomainEntity> {
        return remoteDataSource.createReview(body.toDto()).map { it.toDomainEntity() }
    }

    override suspend fun updateReview(
        reviewId: String,
        body: UpdateReviewRequestDomainEntity
    ): Resource<ReviewDomainEntity> {
        return remoteDataSource.updateReview(reviewId, body.toDto()).map { it.toDomainEntity() }
    }
}

