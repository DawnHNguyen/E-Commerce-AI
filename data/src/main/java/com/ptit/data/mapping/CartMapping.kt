package com.ptit.data.mapper.cart

import com.ptit.data.remote.dto.cart.*
import com.ptit.domain.entity.cart.*

fun ProductCartDto.toDomainEntity() = ProductCartDomainEntity(
    id = id,
    name = name,
    images = images,
    basePrice = basePrice,
    virtualPrice = virtualPrice
)

fun SKUCartDto.toDomainEntity() = SKUCartDomainEntity(
    id = id,
    image = image,
    price = price,
    stock = stock,
    value = value,
    product = product?.toDomainEntity()
)

fun CartItemDto.toDomainEntity() = CartItemDomainEntity(
    id = id,
    quantity = quantity,
    skuId = skuId,
    userId = userId,
    sku = sku?.toDomainEntity(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CartItemDetailDto.toDomainEntity() = CartItemDetailDomainEntity(
    shopId = shop.id,
    shopName = shop.name,
    shopAvatar = shop.avatar,
    cartItems = cartItems.map { it.toDomainEntity() }
)

fun GetCartResponseDto.toDomainEntity() = GetCartDomainEntity(
    data = data.map { it.toDomainEntity() },
    totalItems = totalItems,
    page = page,
    limit = limit,
    totalPages = totalPages
)

fun DeleteCartResponseDto.toDomainEntity() = DeleteCartResponseDomainEntity(
    deletedCount = deletedCount
)