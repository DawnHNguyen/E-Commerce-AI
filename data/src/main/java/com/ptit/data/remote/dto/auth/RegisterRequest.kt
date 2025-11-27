package com.ptit.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirmPassword")
    val confirmPassword: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,
)