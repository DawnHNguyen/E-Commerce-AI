package com.ptit.domain.entity.discount

data class DiscountDomainEntity(
    val id: String,
    val name: String,
    val description: String?,
    val value: Double,
    val code: String,
    val startDate: String,
    val endDate: String,
    val maxUsesPerUser: Int,
    val minOrderValue: Double,
    val maxUses: Int,
    val maxDiscountValue: Double?,
    val displayType: String,
    val voucherType: String,
    val isPlatform: Boolean,
    val shopId: String?,
    val discountApplyType: String,
    val discountStatus: String,
    val discountType: String,
    val currentUses: Int,
    val brands: List<BrandItemDomainEntity>?,
    val categories: List<CategoryItemDomainEntity>?,
    val products: List<ProductItemDomainEntity>?,
    val createdAt: String,
    val updatedAt: String
)

data class BrandItemDomainEntity(
    val id: String,
    val name: String
)

data class CategoryItemDomainEntity(
    val id: String,
    val name: String
)

data class ProductItemDomainEntity(
    val id: String,
    val name: String
)

data class DiscountListDomainEntity(
    val data: List<DiscountDomainEntity>,
    val totalItems: Int? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val totalPages: Int? = null,
    val hasNext: Boolean? = null,
    val hasPrev: Boolean? = null
)

data class ValidateVoucherRequestDomainEntity(
    val code: String,
    val cartItemIds: List<String>? = null
)

data class ValidateVoucherResponseDomainEntity(
    val isValid: Boolean,
    val discount: DiscountDomainEntity? = null,
    val error: String? = null,
    val discountAmount: Double? = null,
    val finalOrderTotal: Double? = null
)

data class ApplyVoucherRequestDomainEntity(
    val code: String,
    val orderId: String? = null,
    val cartItemIds: List<String>? = null
)

data class ApplyVoucherResponseDomainEntity(
    val isValid: Boolean,
    val discount: DiscountDomainEntity? = null,
    val discountAmount: Double? = null,
    val finalOrderTotal: Double? = null,
    val error: String? = null
)

data class CreateDiscountRequestDomainEntity(
    val name: String,
    val description: String?,
    val value: Double,
    val code: String,
    val startDate: String,
    val endDate: String,
    val maxUsesPerUser: Int,
    val minOrderValue: Double,
    val maxUses: Int,
    val maxDiscountValue: Double?,
    val displayType: String,
    val voucherType: String,
    val isPlatform: Boolean,
    val shopId: String?,
    val discountApplyType: String,
    val discountStatus: String,
    val discountType: String,
    val brands: List<String>? = null,
    val categories: List<String>? = null,
    val products: List<String>? = null
)

data class UpdateDiscountRequestDomainEntity(
    val name: String,
    val description: String?,
    val value: Double,
    val code: String,
    val startDate: String,
    val endDate: String,
    val maxUsesPerUser: Int,
    val minOrderValue: Double,
    val maxUses: Int,
    val maxDiscountValue: Double?,
    val displayType: String,
    val voucherType: String,
    val isPlatform: Boolean,
    val shopId: String?,
    val discountApplyType: String,
    val discountStatus: String,
    val discountType: String,
    val brands: List<String>? = null,
    val categories: List<String>? = null,
    val products: List<String>? = null
)

