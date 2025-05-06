package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.OrderApi
import com.ptit.data.remote.dto.order.CreateOrderRequest
import com.ptit.data.remote.dto.order.OrderDetailResponse
import com.ptit.data.remote.dto.order.PayOrderRequest
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class OrderRemoteDataSource @Inject constructor(private val remoteService: OrderApi) {
    suspend fun createOrder(
        purchaseIds: List<String>,
        fullName: String,
        phone: String,
        address: String,
        shippingFee: Int,
        totalAmount: Int,
        note: String = ""
    ) = remoteService.createOrder(
        CreateOrderRequest(
            purchaseIds = purchaseIds,
            fullName = fullName,
            phone = phone,
            address = address,
            shippingFee = shippingFee,
            totalAmount = totalAmount,
            note = note
        )
    )

    suspend fun getOrders() = remoteService.getOrders()

    suspend fun getOrderById(orderId: String): Resource<OrderDetailResponse> {
        return remoteService.getOrderById(orderId)
    }

    suspend fun payOrder(
        orderId: String,
        tokenId: String
    ) = remoteService.payOrder(
        orderId = orderId,
        payOrderRequest = PayOrderRequest(tokenId = tokenId)
    )
}