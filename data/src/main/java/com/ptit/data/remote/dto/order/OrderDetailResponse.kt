package com.ptit.data.remote.dto.order

import com.google.gson.annotations.SerializedName

data class OrderDetailResponse(
    @SerializedName("order")
    val order: OrderDto?,

    @SerializedName("summary")
    val summary: OrderSummaryDto?
)

data class OrderSummaryDto(
    @SerializedName("total_items")
    val totalItems: Int?,

    @SerializedName("total_amount")
    val totalAmount: Int?,

    @SerializedName("shipping_fee")
    val shippingFee: Int?,

    @SerializedName("grand_total")
    val grandTotal: Int?
)