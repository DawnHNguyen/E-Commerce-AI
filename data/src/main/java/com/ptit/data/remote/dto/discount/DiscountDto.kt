package com.ptit.data.remote.dto.discount

import com.google.gson.annotations.SerializedName

data class DiscountDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("value") val value: Double,
    @SerializedName("code") val code: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("maxUsesPerUser") val maxUsesPerUser: Int,
    @SerializedName("minOrderValue") val minOrderValue: Double,
    @SerializedName("maxUses") val maxUses: Int,
    @SerializedName("maxDiscountValue") val maxDiscountValue: Double?,
    @SerializedName("displayType") val displayType: String,
    @SerializedName("voucherType") val voucherType: String,
    @SerializedName("isPlatform") val isPlatform: Boolean,
    @SerializedName("shopId") val shopId: String?,
    @SerializedName("discountApplyType") val discountApplyType: String,
    @SerializedName("discountStatus") val discountStatus: String,
    @SerializedName("discountType") val discountType: String,
    @SerializedName("usesCount") val usesCount: Int?,
    @SerializedName("usersUsed") val usersUsed: List<String>?,
    @SerializedName("brands") val brands: List<BrandItemDto>?,
    @SerializedName("categories") val categories: List<CategoryItemDto>?,
    @SerializedName("products") val products: List<ProductItemDto>?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("createdById") val createdById: String?,
    @SerializedName("updatedById") val updatedById: String?,
    @SerializedName("deletedById") val deletedById: String?,
    @SerializedName("deletedAt") val deletedAt: String?
)

/**
 * DTO for discount detail (có thể có brands/categories/products khi discountApplyType = "SPECIFIC")
 */
data class DiscountDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("value") val value: Double,
    @SerializedName("code") val code: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("maxUsesPerUser") val maxUsesPerUser: Int,
    @SerializedName("minOrderValue") val minOrderValue: Double,
    @SerializedName("maxUses") val maxUses: Int,
    @SerializedName("maxDiscountValue") val maxDiscountValue: Double?,
    @SerializedName("displayType") val displayType: String,
    @SerializedName("voucherType") val voucherType: String,
    @SerializedName("isPlatform") val isPlatform: Boolean,
    @SerializedName("shopId") val shopId: String?,
    @SerializedName("discountApplyType") val discountApplyType: String,
    @SerializedName("discountStatus") val discountStatus: String,
    @SerializedName("discountType") val discountType: String,
    @SerializedName("usesCount") val usesCount: Int?,
    @SerializedName("usersUsed") val usersUsed: List<String>?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("createdById") val createdById: String?,
    @SerializedName("updatedById") val updatedById: String?,
    @SerializedName("deletedById") val deletedById: String?,
    @SerializedName("deletedAt") val deletedAt: String?,
    // Support for SPECIFIC apply type
    @SerializedName("brands") val brands: List<BrandItemDto>?,
    @SerializedName("categories") val categories: List<CategoryItemDto>?,
    @SerializedName("products") val products: List<ProductItemDto>?
)

data class BrandItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class CategoryItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class ProductItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

// Response DTOs
data class GetAvailableDiscountsResponseDto(
    @SerializedName("data") val data: List<DiscountDto>,
    @SerializedName("message") val message: String?,
    @SerializedName("statusCode") val statusCode: Int?
)

data class GetManageDiscountsResponseDto(
    @SerializedName("data") val data: List<DiscountDto>,
    @SerializedName("metadata") val metadata: MetadataDto?,
    @SerializedName("message") val message: String?,
    @SerializedName("statusCode") val statusCode: Int?
)

data class MetadataDto(
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("hasPrev") val hasPrev: Boolean
)

data class GetDiscountDetailResponseDto(
    @SerializedName("data") val data: DiscountDetailDto?,
    @SerializedName("message") val message: String?,
    @SerializedName("statusCode") val statusCode: Int?
)

data class ValidateVoucherCodeResponseDto(
    @SerializedName("isValid") val isValid: Boolean,
    @SerializedName("discount") val discount: DiscountDto?,
    @SerializedName("error") val error: String?,
    @SerializedName("discountAmount") val discountAmount: Double?,
    @SerializedName("finalOrderTotal") val finalOrderTotal: Double?
)

data class ApplyVoucherResponseDto(
    @SerializedName("isValid") val isValid: Boolean,
    @SerializedName("discount") val discount: DiscountDto?,
    @SerializedName("discountAmount") val discountAmount: Double?,
    @SerializedName("finalOrderTotal") val finalOrderTotal: Double?,
    @SerializedName("error") val error: String?
)

data class CreateDiscountResponseDto(
    @SerializedName("data") val data: DiscountDto?,
    @SerializedName("message") val message: String?,
    @SerializedName("statusCode") val statusCode: Int?
)

data class UpdateDiscountResponseDto(
    @SerializedName("data") val data: DiscountDto?,
    @SerializedName("message") val message: String?,
    @SerializedName("statusCode") val statusCode: Int?
)

// Request DTOs
data class ValidateVoucherCodeRequestDto(
    @SerializedName("code") val code: String,
    @SerializedName("cartItemIds") val cartItemIds: List<String>?
)

data class ApplyVoucherRequestDto(
    @SerializedName("code") val code: String,
    @SerializedName("orderId") val orderId: String?,
    @SerializedName("cartItemIds") val cartItemIds: List<String>?
)

data class CreateDiscountRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("value") val value: Double,
    @SerializedName("code") val code: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("maxUsesPerUser") val maxUsesPerUser: Int,
    @SerializedName("minOrderValue") val minOrderValue: Double,
    @SerializedName("maxUses") val maxUses: Int,
    @SerializedName("maxDiscountValue") val maxDiscountValue: Double?, // Required by API
    @SerializedName("displayType") val displayType: String,
    @SerializedName("voucherType") val voucherType: String,
    @SerializedName("isPlatform") val isPlatform: Boolean,
    @SerializedName("shopId") val shopId: String?,
    @SerializedName("discountApplyType") val discountApplyType: String,
    @SerializedName("discountStatus") val discountStatus: String,
    @SerializedName("discountType") val discountType: String,
    @SerializedName("brands") val brands: List<String>?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("products") val products: List<String>?
)

data class UpdateDiscountRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("value") val value: Double,
    @SerializedName("code") val code: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("maxUsesPerUser") val maxUsesPerUser: Int,
    @SerializedName("minOrderValue") val minOrderValue: Double,
    @SerializedName("maxUses") val maxUses: Int,
    @SerializedName("maxDiscountValue") val maxDiscountValue: Double, // Required by API
    @SerializedName("displayType") val displayType: String,
    @SerializedName("voucherType") val voucherType: String,
    @SerializedName("isPlatform") val isPlatform: Boolean,
    @SerializedName("shopId") val shopId: String?,
    @SerializedName("discountApplyType") val discountApplyType: String,
    @SerializedName("discountStatus") val discountStatus: String,
    @SerializedName("discountType") val discountType: String,
    @SerializedName("brands") val brands: List<String>?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("products") val products: List<String>?
)

data class MessageResponseDto(
    @SerializedName("message") val message: String
)

