package com.ptit.data.mapping

import com.ptit.data.remote.dto.home.ListProductResponse
import com.ptit.domain.entity.home.ListProductDomainEntity

fun ListProductResponse.toDomainEntity() = ListProductDomainEntity(
    products = products?.map { it.toDomainEntity() } ?: emptyList(),
)