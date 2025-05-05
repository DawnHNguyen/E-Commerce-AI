package com.ptit.data.remote.dto.order

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.cart.PurchaseDto

data class OrderDto(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("purchases")
    val purchases: List<PurchaseDto>?,

    @SerializedName("shipping_fee")
    val shippingFee: Int?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("user")
    val userId: String?,

    @SerializedName("full_name")
    val fullName: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("address")
    val address: String?,

    @SerializedName("total_amount")
    val totalAmount: Int?,

    @SerializedName("payment_method")
    val paymentMethod: String?,

    @SerializedName("note")
    val note: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("paid_at")
    val paidAt: String?,

    @SerializedName("payment_gateway_response")
    val paymentGatewayResponse: String?,

    @SerializedName("recurly_account_id")
    val recurlyAccountId: String?,

    @SerializedName("recurly_transaction_id")
    val recurlyTransactionId: String?
)



