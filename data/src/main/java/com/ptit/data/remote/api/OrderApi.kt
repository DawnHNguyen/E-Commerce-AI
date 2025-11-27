package com.ptit.data.remote.api

import com.ptit.data.remote.dto.order.CancelOrderResponseDto
import com.ptit.data.remote.dto.order.CreateOrderDataDto
import com.ptit.data.remote.dto.order.CreateOrderRequestDto
import com.ptit.data.remote.dto.order.GetOrderListResponseDto
import com.ptit.data.remote.dto.order.OrderDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {

    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10,
        @Query("status") status: String? = null
    ): Resource<GetOrderListResponseDto>

    @GET("orders/{orderId}")
    suspend fun getOrderById(
        @Path("orderId") orderId: String
    ): Resource<OrderDto>

    @POST("orders")
    suspend fun createOrder(
        @Body createOrderRequest: CreateOrderRequestDto
    ): Resource<CreateOrderDataDto>

    @PUT("orders/{orderId}")
    suspend fun cancelOrder(
        @Path("orderId") orderId: String
    ): Resource<CancelOrderResponseDto>
}