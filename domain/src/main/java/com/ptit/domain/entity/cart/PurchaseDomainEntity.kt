package com.ptit.domain.entity.cart

import com.ptit.domain.entity.product.ProductDomainEntity

data class PurchaseDomainEntity(
    val id: String = "",
    val buyCount: Int = 0,
    val price: Int = 0,
    val priceBeforeDiscount: Int = 0,
    val status: Int = 0,
    val user: String = "",
    val product: ProductDomainEntity = ProductDomainEntity(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

