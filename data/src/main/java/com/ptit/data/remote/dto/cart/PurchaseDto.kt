package com.ptit.data.remote.dto.cart

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.product.ProductDto

data class PurchaseDto(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("buy_count")
    val buyCount: Int?,

    @SerializedName("price")
    val price: Int?,

    @SerializedName("price_before_discount")
    val priceBeforeDiscount: Int?,

    @SerializedName("status")
    val status: Int?,

    @SerializedName("user")
    val user: String?,

    @SerializedName("product")
    val product: ProductDto?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)