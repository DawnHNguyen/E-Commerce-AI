package com.ptit.data.remote.dto.address

import com.google.gson.annotations.SerializedName

data class AddressRootResponse(
    @SerializedName("data")
    val data: AddressDataWrapper?,
    @SerializedName("statusCode")
    val statusCode: Int?
)

// 2. Class này hứng object bên trong "data": {"message": "...", "data": [...]}
data class AddressDataWrapper(
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val addresses: List<AddressDto>? // Đây mới là list địa chỉ thật
)
data class CreateAddressResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: AddressDto?
)

