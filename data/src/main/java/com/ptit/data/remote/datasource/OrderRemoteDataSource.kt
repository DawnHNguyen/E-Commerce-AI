package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.OrderApi
import com.ptit.data.remote.dto.order.CreateOrderRequestDto
import com.ptit.data.remote.dto.order.UpdateOrderStatusRequest

import javax.inject.Inject

class OrderRemoteDataSource @Inject constructor(private val api: OrderApi) {
    suspend fun getOrders(page: Int? = 1, limit: Int? = 10, status: String? = null) =
        api.getOrders(page, limit, status)

    suspend fun getOrderById(orderId: String) = api.getOrderById(orderId)

    suspend fun createOrder(request: CreateOrderRequestDto) = api.createOrder(request)

    suspend fun cancelOrder(orderId: String) = api.cancelOrder(orderId)

    // ----------------------------------------------------
    // 🛍️ IMPLEMENT API CHO CHỦ SHOP
    // ----------------------------------------------------

    suspend fun getManageOrders(page: Int? = 1, limit: Int? = 10, status: String? = null) =
        api.getManageOrders(page, limit, status)

    suspend fun getManageOrderDetail(orderId: String) =
        api.getManageOrderDetail(orderId)

    suspend fun updateOrderStatus(orderId: String, status: String) =
        api.updateOrderStatus(orderId, UpdateOrderStatusRequest(status))
}