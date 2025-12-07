package com.ptit.data.remote.dto.seller_request

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.common.UserDto

// Seller Request DTO
data class SellerRequestDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("shopName")
    val shopName: String,
    @SerializedName("shopDescription")
    val shopDescription: String?,
    @SerializedName("businessLicense")
    val businessLicense: String?,
    @SerializedName("taxCode")
    val taxCode: String?,
    @SerializedName("status")
    val status: String, // PENDING, APPROVED, REJECTED
    @SerializedName("rejectionReason")
    val rejectionReason: String?,
    @SerializedName("approvedById")
    val approvedById: String?,
    @SerializedName("approvedAt")
    val approvedAt: String?,
    @SerializedName("rejectedById")
    val rejectedById: String?,
    @SerializedName("rejectedAt")
    val rejectedAt: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("user")
    val user: UserDto?
)

// Create Seller Request (POST /seller-requests)
data class CreateSellerRequestBody(
    @SerializedName("shopName")
    val shopName: String,
    @SerializedName("shopDescription")
    val shopDescription: String?,
    @SerializedName("businessLicense")
    val businessLicense: String?,
    @SerializedName("taxCode")
    val taxCode: String?
)

data class CreateSellerRequestResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: SellerRequestDto
)

// Get My Seller Request (GET /seller-requests/my-request)
data class GetMySellerRequestResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: SellerRequestDto?
)

// Get Seller Requests List (GET /seller-requests) - Admin
data class GetSellerRequestsResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<SellerRequestDto>,
    @SerializedName("metadata")
    val metadata: PaginationMetadata
)

data class PaginationMetadata(
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("hasPrev")
    val hasPrev: Boolean
)

// Reject Seller Request Body (PUT /seller-requests/:requestId/reject)
data class RejectSellerRequestBody(
    @SerializedName("rejectionReason")
    val rejectionReason: String
)

// Approve/Reject Response
data class ApproveSellerRequestResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: SellerRequestDto
)

