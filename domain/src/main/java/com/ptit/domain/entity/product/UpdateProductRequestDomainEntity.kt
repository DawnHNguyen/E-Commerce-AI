package com.ptit.domain.entity.product

data class UpdateProductRequestDomainEntity(
    val name: String,
    val description: String = "",
    val publishedAt: String? = null,
    val basePrice: Int,
    val virtualPrice: Int? = null,
    val brandId: String? = null,
    val categoryId: String,
    val images: List<String> = emptyList(),
    val variants: List<VariantRequestDomainEntity> = emptyList(),
    val skus: List<SKURequestDomainEntity> = emptyList(),
    val specifications: List<SpecificationDomainEntity> = emptyList()
)
