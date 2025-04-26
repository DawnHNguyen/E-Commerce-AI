package com.ptit.data.remote.dto.common

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("shop")
    val shop: Shop?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("watchList")
    val watchList: List<String>?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("avatar")
    val avatar: String?,
) {
    data class Shop(
        @SerializedName("address")
        val address: String?,
        @SerializedName("avatar")
        val avatar: String?,
        @SerializedName("description")
        val description: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("phone")
        val phone: String?,
    )
}
