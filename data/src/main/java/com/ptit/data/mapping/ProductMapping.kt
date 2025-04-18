package com.ptit.data.mapping

import com.ptit.data.remote.dto.product.CategoryDto
import com.ptit.data.remote.dto.product.ProductDto
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity

fun ProductDto.toDomainEntity() = ProductDomainEntity(
    id = id ?: "",
    name = name ?: "",
    price = price ?: 0,
    category = category?.toDomainEntity() ?: CategoryDomainEntity(),
    image = image ?: "",
    rating = rating ?: 0f,
    sold = sold ?: 0,
    createdAt = createdAt ?: "",
    images = images ?: emptyList(),
    priceBeforeDiscount = priceBeforeDiscount ?: 0,
    quantity = quantity ?: 0,
    shop = shop ?: "",
    updatedAt = updatedAt ?: "",
    view = view ?: 0,

    )

fun CategoryDto.toDomainEntity() = CategoryDomainEntity(
    id = id ?: "",
    name = name ?: "",
)