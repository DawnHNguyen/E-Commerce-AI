package com.ptit.data.mapping

import com.ptit.data.remote.dto.product.CategoryDto
import com.ptit.data.remote.dto.product.GetAllCategoriesResDto
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.GetAllCategoriesDomainEntity

fun CategoryDto.toDomainEntity(): CategoryDomainEntity {
    return CategoryDomainEntity(
        id = id ?: "",
        name = name ?: "",
        logo = logo,
        parentCategoryId = parentCategoryId,
        createdById = createdById,
        updatedById = updatedById,
        deletedById = deletedById,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun GetAllCategoriesResDto.toDomainEntity(): GetAllCategoriesDomainEntity {
    return GetAllCategoriesDomainEntity(
        data = data?.map { it.toDomainEntity() } ?: emptyList(),
        totalItems = totalItems ?: 0
    )
}


