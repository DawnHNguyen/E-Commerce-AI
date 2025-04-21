package com.ptit.data.remote.dto.cart

import com.google.gson.annotations.SerializedName

data class PurchaseListResponse(
    @SerializedName("statusCode")
    val statusCode: Int?,

    @SerializedName("data")
    val data: List<PurchaseDto>? = emptyList(),
)