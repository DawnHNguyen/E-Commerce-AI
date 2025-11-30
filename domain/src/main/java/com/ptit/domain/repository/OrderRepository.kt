package com.ptit.domain.repository

import com.ptit.domain.entity.order.CancelOrderResponseDomainEntity
import com.ptit.domain.entity.order.CreateOrderRequestDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.GetOrderListDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.utils.Resource

interface OrderRepository {
    suspend fun getOrders(
        page: Int? = 1,
        limit: Int? = 10,
        status: String? = null
    ): Resource<GetOrderListDomainEntity>

    suspend fun getMyOrders(): Resource<List<OrderDomainEntity>>

    suspend fun getOrderById(orderId: String): Resource<OrderDomainEntity>

    suspend fun createOrder(
        request: CreateOrderRequestDomainEntity
    ): Resource<CreateOrderResponseDomainEntity>

    suspend fun cancelOrder(orderId: String): Resource<CancelOrderResponseDomainEntity>
}