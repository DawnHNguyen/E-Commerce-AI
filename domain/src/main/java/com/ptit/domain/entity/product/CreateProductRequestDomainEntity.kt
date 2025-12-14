package com.ptit.domain.entity.product

data class SpecificationDomainEntity(
    val name: String,
    val value: String
)

data class CreateProductRequestDomainEntity(
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

data class VariantRequestDomainEntity(
    val name: String,
    val options: List<String>
)

data class SKURequestDomainEntity(
    val value: String,
    val price: Int,
    val stock: Int,
    val image: String = ""
)
