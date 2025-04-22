package com.ptit.data.remote.dto.cart

import com.google.gson.annotations.SerializedName

data class UpdatePurchaseRequestDto(
    @SerializedName("product_id")
    val productId: String,

    @SerializedName("buy_count")
    val buyCount: Int
)