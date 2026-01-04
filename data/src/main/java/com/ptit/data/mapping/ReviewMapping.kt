package com.ptit.data.mapping

import com.ptit.data.remote.dto.review.*
import com.ptit.domain.entity.review.*

fun ReviewMediaDto.toDomainEntity() = ReviewMediaDomainEntity(
    id = id,
    url = url,
    type = type,
    reviewId = reviewId,
    createdAt = createdAt
)

fun ReviewUserDto.toDomainEntity() = ReviewUserDomainEntity(
    id = id,
    name = name,
    avatar = avatar
)

fun ReviewDto.toDomainEntity() = ReviewDomainEntity(
    id = id,
    content = content,
    rating = rating,
    orderId = orderId,
    productId = productId,
    userId = userId,
    updateCount = updateCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    medias = medias?.map { it.toDomainEntity() },
    user = user?.toDomainEntity()
)

fun CreateReviewResponseDto.toDomainEntity() = ReviewDomainEntity(
    id = id,
    content = content,
    rating = rating,
    orderId = orderId,
    productId = productId,
    userId = userId,
    updateCount = updateCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    medias = medias.map { it.toDomainEntity() },
    user = user.toDomainEntity()
)

fun UpdateReviewResponseDto.toDomainEntity() = ReviewDomainEntity(
    id = id,
    content = content,
    rating = rating,
    orderId = orderId,
    productId = productId,
    userId = userId,
    updateCount = updateCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    medias = medias.map { it.toDomainEntity() },
    user = user.toDomainEntity()
)

fun GetReviewsResponseDto.toDomainEntity() = GetReviewsResponseDomainEntity(
    data = data.map { it.toDomainEntity() },
    totalItems = totalItems,
    page = page,
    limit = limit,
    totalPages = totalPages
)

fun CreateReviewMediaDomainEntity.toDto() = CreateReviewMediaDto(
    url = url,
    type = type
)

fun CreateReviewRequestDomainEntity.toDto() = CreateReviewRequestDto(
    content = content,
    rating = rating,
    productId = productId,
    orderId = orderId,
    medias = medias.map { it.toDto() }
)

fun UpdateReviewRequestDomainEntity.toDto() = UpdateReviewRequestDto(
    content = content,
    rating = rating,
    productId = productId,
    orderId = orderId,
    medias = medias.map { it.toDto() }
)

