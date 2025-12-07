package com.ptit.data.remote.dto.shop

import com.google.gson.annotations.SerializedName

data class ShopDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class CreateShopRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("avatar")
    val avatar: String?
)

data class UpdateShopRequest(
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("avatar")
    val avatar: String?
)

data class ShopResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ShopDto
)

