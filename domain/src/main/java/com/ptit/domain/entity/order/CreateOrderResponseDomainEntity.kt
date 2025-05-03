package com.ptit.domain.entity.order

data class CreateOrderResponseDomainEntity(
    val orderId: String,
    val totalAmount: Int
)