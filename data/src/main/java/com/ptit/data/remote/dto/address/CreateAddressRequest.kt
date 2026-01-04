package com.ptit.data.remote.dto.address

import com.google.gson.annotations.SerializedName

data class CreateAddressRequest(
    @SerializedName("name")
    val name: String, // Tên gợi nhớ

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

    @SerializedName("provinceId")
    val provinceId: Int,

    @SerializedName("districtId")
    val districtId: Int,

    @SerializedName("wardCode")
    val wardCode: String,

    @SerializedName("street")
    val street: String,

    @SerializedName("addressType")
    val addressType: String = "HOME",

    @SerializedName("isDefault")
    val isDefault: Boolean = false
)