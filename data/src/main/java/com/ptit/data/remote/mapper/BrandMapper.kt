package com.ptit.data.remote.mapper

import com.ptit.data.remote.dto.brand.BrandDto
import com.ptit.data.remote.dto.brand.CreateBrandRequestDto
import com.ptit.data.remote.dto.brand.GetBrandDetailResponseDto
import com.ptit.data.remote.dto.brand.GetBrandsResponseDto
import com.ptit.data.remote.dto.brand.UpdateBrandRequestDto
import com.ptit.domain.entity.brand.BrandDomainEntity
import com.ptit.domain.entity.brand.BrandListDomainEntity
import com.ptit.domain.entity.brand.CreateBrandRequestDomainEntity
import com.ptit.domain.entity.brand.UpdateBrandRequestDomainEntity

// DTO to Domain Entity
fun BrandDto.toDomainEntity(): BrandDomainEntity {
    return BrandDomainEntity(
        id = id,
        name = name,
        logo = logo,
        createdById = createdById,
        updatedById = updatedById,
        deletedById = deletedById,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        discountId = discountId
    )
}

fun GetBrandsResponseDto.toDomainEntity(): BrandListDomainEntity {
    return BrandListDomainEntity(
        data = data.map { it.toDomainEntity() },
        totalItems = totalItems,
        page = page,
        limit = limit,
        totalPages = totalPages
    )
}

fun GetBrandDetailResponseDto.toDomainEntity(): BrandDomainEntity {
    return data.toDomainEntity()
}

// Domain Entity to DTO
fun CreateBrandRequestDomainEntity.toDto(): CreateBrandRequestDto {
    return CreateBrandRequestDto(
        name = name,
        logo = logo
    )
}

fun UpdateBrandRequestDomainEntity.toDto(): UpdateBrandRequestDto {
    return UpdateBrandRequestDto(
        name = name,
        logo = logo
    )
}

