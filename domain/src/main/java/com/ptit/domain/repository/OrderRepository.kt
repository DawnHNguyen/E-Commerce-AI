package com.ptit.domain.repository

import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.utils.Resource

interface OrderRepository {
    suspend fun createOrder(
        purchaseIds: List<String>,
        fullName: String,
        phone: String,
        address: String,
        note: String = "",
        totalAmount: Int,
        shippingFee: Int
    ): Resource<CreateOrderResponseDomainEntity>

    suspend fun getOrders(): Resource<List<OrderDomainEntity>>

    suspend fun getOrderById(orderId: String): Resource<OrderDomainEntity>
}