package com.ptit.data.remote.dto.review

import com.google.gson.annotations.SerializedName

data class ReviewMediaDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("type")
    val type: String, // "IMAGE" or "VIDEO"
    @SerializedName("reviewId")
    val reviewId: String,
    @SerializedName("createdAt")
    val createdAt: String
)

data class ReviewUserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String?
)

data class ReviewDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("updateCount")
    val updateCount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("medias")
    val medias: List<ReviewMediaDto>? = null,
    @SerializedName("user")
    val user: ReviewUserDto? = null
)

data class CreateReviewMediaDto(
    @SerializedName("url")
    val url: String,
    @SerializedName("type")
    val type: String
)

data class CreateReviewRequestDto(
    @SerializedName("content")
    val content: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("medias")
    val medias: List<CreateReviewMediaDto> = emptyList()
)

data class CreateReviewResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("updateCount")
    val updateCount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("medias")
    val medias: List<ReviewMediaDto>,
    @SerializedName("user")
    val user: ReviewUserDto
)

data class UpdateReviewRequestDto(
    @SerializedName("content")
    val content: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("medias")
    val medias: List<CreateReviewMediaDto> = emptyList()
)

data class UpdateReviewResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("updateCount")
    val updateCount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("medias")
    val medias: List<ReviewMediaDto>,
    @SerializedName("user")
    val user: ReviewUserDto
)

data class GetReviewsResponseDto(
    @SerializedName("data")
    val data: List<ReviewDto>,
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

// Wrapper for backend response that has nested data structure
data class GetReviewsResponseWrapperDto(
    @SerializedName("data")
    val data: GetReviewsResponseDto,
    @SerializedName("statusCode")
    val statusCode: Int
)

