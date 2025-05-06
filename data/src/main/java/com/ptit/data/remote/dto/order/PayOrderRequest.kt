package com.ptit.data.remote.dto.order

import com.google.gson.annotations.SerializedName

data class PayOrderRequest(
    @SerializedName("token_id")
    val tokenId: String
)
