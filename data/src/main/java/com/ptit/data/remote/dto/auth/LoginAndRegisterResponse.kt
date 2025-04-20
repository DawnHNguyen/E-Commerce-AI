package com.ptit.data.remote.dto.auth

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.common.UserDto

data class LoginAndRegisterResponse(
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("expires")
    val expires: String?,
    @SerializedName("user")
    val user: UserDto?,
)