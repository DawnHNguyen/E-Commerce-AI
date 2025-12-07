package com.ptit.data.remote.dto.address

import com.google.gson.annotations.SerializedName

data class GetAddressesResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<AddressDto>?
)

data class CreateAddressResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: AddressDto?
)

