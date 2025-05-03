package com.ptit.data.mapping

import com.ptit.data.remote.dto.order.CreateOrderResponse
import com.ptit.data.remote.dto.order.OrderDto
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity

fun OrderDto.toDomainEntity() = OrderDomainEntity(
    id = id ?: "",
    purchases = purchases?.map { it.toDomainEntity() } ?: emptyList(),
    shippingFee = shippingFee ?: 0,
    status = status ?: "",
    userId = userId ?: "",
    fullName = fullName ?: "",
    phone = phone ?: "",
    address = address ?: "",
    totalAmount = totalAmount ?: 0,
    paymentMethod = paymentMethod ?: "",
    note = note ?: "",
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: "",
    paidAt = paidAt,
    paymentGatewayResponse = paymentGatewayResponse,
    recurlyAccountId = recurlyAccountId,
    recurlyTransactionId = recurlyTransactionId
)

fun CreateOrderResponse.toDomainEntity() = CreateOrderResponseDomainEntity(
    orderId = orderId ?: "",
    totalAmount = totalAmount ?: 0,
)
