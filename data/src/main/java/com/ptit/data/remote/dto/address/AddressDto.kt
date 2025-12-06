package com.ptit.data.remote.dto.address

import com.google.gson.annotations.SerializedName

data class AddressDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("recipient")
    val recipient: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("province")
    val province: String,
    @SerializedName("district")
    val district: String,
    @SerializedName("ward")
    val ward: String,
    @SerializedName("street")
    val street: String,
    @SerializedName("isDefault")
    val isDefault: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

