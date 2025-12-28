package com.ptit.domain.entity.recommendation

data class RecommendedProductDomainEntity(
    val id: String,
    val name: String,
    val description: String,
    val basePrice: Int,
    val virtualPrice: Int,
    val images: List<String>,
    val categoryId: String?,
    val brandId: String,
    val score: Double,
    val brandName: String,
    val brandLogo: String,
    val categoryName: String?
)

