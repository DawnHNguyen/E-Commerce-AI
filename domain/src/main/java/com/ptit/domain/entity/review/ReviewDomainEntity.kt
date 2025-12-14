package com.ptit.domain.entity.review

data class ReviewMediaDomainEntity(
    val id: String,
    val url: String,
    val type: String, // "IMAGE" or "VIDEO"
    val reviewId: String,
    val createdAt: String
)

data class ReviewUserDomainEntity(
    val id: String,
    val name: String,
    val avatar: String?
)

data class ReviewDomainEntity(
    val id: String,
    val content: String,
    val rating: Int,
    val orderId: String,
    val productId: String,
    val userId: String,
    val updateCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val medias: List<ReviewMediaDomainEntity>? = null,
    val user: ReviewUserDomainEntity? = null
)

data class CreateReviewMediaDomainEntity(
    val url: String,
    val type: String
)

data class CreateReviewRequestDomainEntity(
    val content: String,
    val rating: Int,
    val productId: String,
    val orderId: String,
    val medias: List<CreateReviewMediaDomainEntity> = emptyList()
)

data class UpdateReviewRequestDomainEntity(
    val content: String,
    val rating: Int,
    val productId: String,
    val orderId: String,
    val medias: List<CreateReviewMediaDomainEntity> = emptyList()
)

data class GetReviewsResponseDomainEntity(
    val data: List<ReviewDomainEntity>,
    val totalItems: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

