package com.ptit.data.remote.dto.order

import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("purchase_ids")
    val purchaseIds: List<String>?,

    @SerializedName("shipping_fee")
    val shippingFee: Int?,

    @SerializedName("full_name")
    val fullName: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("address")
    val address: String?,

    @SerializedName("payment_method")
    val paymentMethod: String = "recurly",

    @SerializedName("note")
    val note: String,

    @SerializedName("total_amount")
    val totalAmount: Int?
)