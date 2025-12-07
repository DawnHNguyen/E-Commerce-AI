package com.ptit.data.remote.dto.profile

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.common.UserDto

data class UpdateProfileResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: UserDto?,
    @SerializedName("statusCode")
    val statusCode: Int?
)

