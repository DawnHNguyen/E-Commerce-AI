package com.ptit.domain.entity.home

import com.ptit.domain.entity.product.ProductDomainEntity

data class ListProductDomainEntity(
    val products: List<ProductDomainEntity> = emptyList(),
)
