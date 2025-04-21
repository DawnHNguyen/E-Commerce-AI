package com.ptit.data.mapping

import com.ptit.data.remote.dto.cart.PurchaseDto
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity

fun PurchaseDto.toDomainEntity() = PurchaseDomainEntity(
    id = id ?: "",
    buyCount = buyCount ?: 0,
    price = price ?: 0,
    priceBeforeDiscount = priceBeforeDiscount ?: 0,
    status = status ?: 0,
    user = user ?: "",
    product = product?.toDomainEntity() ?: ProductDomainEntity(),
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: ""
)
