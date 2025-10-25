package com.ptit.domain.entity.product

data class UpdateProductRequestDomainEntity(
    val name: String,
    val basePrice: Int,
    val virtualPrice: Int? = null,
    val brandId: String? = null,
    val categoryId: String? = null,
    val images: List<String> = emptyList(),
    val variants: List<VariantDomainEntity> = emptyList(),
    val skus: List<SKUDomainEntity> = emptyList(),
    val publishedAt: String? = null,
    val description: String = ""
)
