package com.ptit.data.remote.dto.order

import com.google.gson.annotations.SerializedName

data class CreateOrderResponse (
    @SerializedName("orderId")
    val orderId: String?,

    @SerializedName("total_amount")
    val totalAmount: Int?
)

