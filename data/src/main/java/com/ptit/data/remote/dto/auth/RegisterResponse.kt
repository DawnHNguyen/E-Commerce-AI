package com.ptit.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("roleId")
    val roleId: String,

    @SerializedName("isTwoFactorEnabled")
    val isTwoFactorEnabled: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)