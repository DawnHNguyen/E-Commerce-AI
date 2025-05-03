package com.ptit.data.remote.api

import com.ptit.data.remote.dto.order.CreateOrderRequest
import com.ptit.data.remote.dto.order.CreateOrderResponse
import com.ptit.data.remote.dto.order.OrderDetailResponse
import com.ptit.data.remote.dto.order.OrderDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(
        @Body createOrderRequest: CreateOrderRequest
    ): Resource<CreateOrderResponse>

    @GET("orders")
    suspend fun getOrders(): Resource<List<OrderDto>>

    @GET("orders/{orderId}")
    suspend fun getOrderById(
        @Path("orderId") orderId: String
    ): Resource<OrderDetailResponse>
}