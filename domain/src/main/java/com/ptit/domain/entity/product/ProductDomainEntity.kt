package com.ptit.domain.entity.product

import com.ptit.domain.entity.common.UserDomainEntity

data class ProductDomainEntity(
    val id: String = "",
    val name: String = "",
    val basePrice: Int = 0,
    val virtualPrice: Int? = null,
    val images: List<String> = emptyList(),
    val variants: List<VariantDomainEntity> = emptyList(),
    val skus: List<SKUDomainEntity> = emptyList(),
    val category: CategoryDomainEntity? = null,
    val brand: BrandDomainEntity? = null,
    val createdById: String = "",
    val shop: UserDomainEntity? = null,
    val isPublic: Boolean = false,
    val publishedAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val sold: Int = 0,
    val rating: Float = 0f,
    val view: Int = 0,
    val description: String = ""
) {
    val hasDiscount: Boolean
        get() = virtualPrice != null && virtualPrice > basePrice && virtualPrice > 0

    val discountPercent: Int
        get() = if (hasDiscount && virtualPrice != null) {
            ((virtualPrice.toFloat() - basePrice) * 100 / virtualPrice).toInt()
        } else {
            0
        }
}

data class SKUDomainEntity(
    val id: String = "",
    val value: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val image: String = ""
)

data class VariantDomainEntity(
    val name: String = "",
    val options: List<String> = emptyList()
)

data class BrandDomainEntity(
    val id: String = "",
    val name: String = "",
)
