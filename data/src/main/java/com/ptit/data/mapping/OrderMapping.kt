package com.ptit.data.mapping

import com.ptit.data.remote.dto.order.CancelOrderResponseDto
import com.ptit.data.remote.dto.order.CreateOrderResponseDto
import com.ptit.data.remote.dto.order.GetOrderListResponseDto
import com.ptit.data.remote.dto.order.OrderDto
import com.ptit.data.remote.dto.order.ProductSKUSnapshotDto
import com.ptit.data.remote.dto.order.ReceiverDto
import com.ptit.domain.entity.order.CancelOrderResponseDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.GetOrderListDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.entity.order.ProductSKUSnapshotDomainEntity
import com.ptit.domain.entity.order.ReceiverDomainEntity

fun ProductSKUSnapshotDto.toDomainEntity() = ProductSKUSnapshotDomainEntity(
    id = id,
    productId = productId,
    productName = productName,
    skuPrice = skuPrice,
    image = image,
    skuValue = skuValue,
    skuId = skuId,
    orderId = orderId,
    quantity = quantity,
    createdAt = createdAt
)

fun ReceiverDto.toDomainEntity() = ReceiverDomainEntity(
    name = name,
    phone = phone,
    address = address
)

fun OrderDto.toDomainEntity() = OrderDomainEntity(
    id = id,
    userId = userId,
    shopId = shopId ?: "",
    status = status,
    totalAmount = totalAmount,
    paymentMethod = paymentMethod ?: "",
    receiver = receiver?.toDomainEntity(),
    items = items.map { it.toDomainEntity() },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun GetOrderListResponseDto.toDomainEntity() = GetOrderListDomainEntity(
    data = data.map { it.toDomainEntity() },
    totalItems = totalItems,
    page = page,
    limit = limit,
    totalPages = totalPages
)

fun CreateOrderResponseDto.toDomainEntity() = CreateOrderResponseDomainEntity(
    orders = orders.map { it.toDomainEntity() },
    paymentId = paymentId
)

fun CancelOrderResponseDto.toDomainEntity() = CancelOrderResponseDomainEntity(
    id = id,
    status = status
)