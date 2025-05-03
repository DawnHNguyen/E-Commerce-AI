package com.ptit.data.remote.dto.shop

import com.google.gson.annotations.SerializedName

data class CreateAndUpdateShopRequest (
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("phone")
    val phone: String,
)