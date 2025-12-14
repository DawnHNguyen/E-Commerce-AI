package com.ptit.domain.entity.brand

data class BrandDomainEntity(
    val id: String,
    val name: String,
    val logo: String,
    val createdById: String? = null,
    val updatedById: String? = null,
    val deletedById: String? = null,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val discountId: String? = null
)

data class CreateBrandRequestDomainEntity(
    val name: String,
    val logo: String
)

data class UpdateBrandRequestDomainEntity(
    val name: String,
    val logo: String
)

data class BrandListDomainEntity(
    val data: List<BrandDomainEntity>,
    val totalItems: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

