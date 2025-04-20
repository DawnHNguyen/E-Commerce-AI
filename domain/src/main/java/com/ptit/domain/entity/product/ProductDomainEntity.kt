package com.ptit.domain.entity.product

import com.ptit.domain.entity.common.UserDomainEntity

data class ProductDomainEntity(
    val category: CategoryDomainEntity = CategoryDomainEntity(),
    val createdAt: String = "",
    val id: String = "",
    val image: String = "",
    val images: List<String> = emptyList(),
    val name: String = "",
    val price: Int = 0,
    val priceBeforeDiscount: Int = 0,
    val quantity: Int = 0,
    val rating: Float = 0f,
    val shop: UserDomainEntity = UserDomainEntity(),
    val sold: Int = 0,
    val updatedAt: String = "",
    val view: Int = 0,
    val description: String = "",
) {
    val hasDiscount get() = lazy {
        priceBeforeDiscount > price && priceBeforeDiscount > 0
    }

    val discountPercent get() = lazy {
        if (hasDiscount.value) {
            ((priceBeforeDiscount.toFloat() - price) * 100 / priceBeforeDiscount).toInt()
        } else {
            0
        }
    }
}
