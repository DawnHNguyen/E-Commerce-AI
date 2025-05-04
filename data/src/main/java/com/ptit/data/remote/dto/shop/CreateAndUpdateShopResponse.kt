package com.ptit.data.remote.dto.shop

import com.google.gson.annotations.SerializedName

data class CreateAndUpdateShopResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("totalProduct")
    val totalProduct: Int? = 0,
    @SerializedName("totalOrder")
    val totalOrder: Int? = 0,
)
