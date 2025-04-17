package com.ptit.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class LoginAndRegisterRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
)