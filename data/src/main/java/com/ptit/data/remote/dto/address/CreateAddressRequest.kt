package com.ptit.data.remote.dto.address

import com.google.gson.annotations.SerializedName

data class CreateAddressRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("recipient")
    val recipient: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("provinceId")
    val provinceId: Int,
    @SerializedName("districtId")
    val districtId: Int,
    @SerializedName("wardCode")
    val wardCode: String,
    @SerializedName("street")
    val street: String,
    @SerializedName("addressType")
    val addressType: String, // "HOME", "OFFICE", or "OTHER"
    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

