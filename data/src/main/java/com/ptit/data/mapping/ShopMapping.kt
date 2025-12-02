package com.ptit.data.mapping

import com.ptit.data.remote.dto.shop.ShopDto
import com.ptit.domain.entity.shop.ShopDomainEntity

fun ShopDto.toDomainEntity() = ShopDomainEntity(
    id = id,
    name = name,
    description = description,
    address = address,
    phone = phone,
    avatar = avatar,
    userId = userId,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)