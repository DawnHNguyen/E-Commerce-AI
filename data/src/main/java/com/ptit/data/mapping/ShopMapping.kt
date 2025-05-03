package com.ptit.data.mapping

import com.ptit.data.remote.dto.shop.CreateAndUpdateShopResponse
import com.ptit.domain.entity.shop.ShopDomainEntity

fun CreateAndUpdateShopResponse.toDomainEntity() = ShopDomainEntity(
    name = name ?: "",
    address = address ?: "",
    phone = phone ?: "",
    description = description ?: "",
    avatar = avatar ?: "",
)