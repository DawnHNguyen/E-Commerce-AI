package com.ptit.domain.entity.product

data class CategoryDomainEntity(
    val id: String = "",
    val name: String = "",
    val logo: String? = null,
    val parentCategoryId: String? = null,
    val createdById: String? = null,
    val updatedById: String? = null,
    val deletedById: String? = null,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class GetAllCategoriesDomainEntity(
    val data: List<CategoryDomainEntity> = emptyList(),
    val totalItems: Int = 0
)
