package com.ptit.data.mapping

import com.ptit.data.remote.dto.common.UserDto
import com.ptit.domain.entity.common.UserDomainEntity

fun UserDto.toDomainEntity() = UserDomainEntity(
    id = id ?: "",
    email = email ?: "",
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: "",
    roles = roles ?: emptyList(),
    watchList = watchList ?: emptyList(),
    shop = shop?.toDomainEntity() ?: UserDomainEntity.Shop(),
)

fun UserDto.Shop.toDomainEntity() = UserDomainEntity.Shop(
    address = address ?: "",
    avatar = avatar ?: "",
    description = description ?: "",
    name = name ?: "",
    phone = phone ?: ""
)