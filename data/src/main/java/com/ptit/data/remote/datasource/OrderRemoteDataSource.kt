package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.OrderApi
import com.ptit.data.remote.dto.order.CreateOrderRequestDto

import javax.inject.Inject

class OrderRemoteDataSource @Inject constructor(private val api: OrderApi) {
    suspend fun getOrders(page: Int? = 1, limit: Int? = 10, status: String? = null) =
        api.getOrders(page, limit, status)

    suspend fun getOrderById(orderId: String) = api.getOrderById(orderId)

    suspend fun createOrder(request: CreateOrderRequestDto) = api.createOrder(request)

    suspend fun cancelOrder(orderId: String) = api.cancelOrder(orderId)
}