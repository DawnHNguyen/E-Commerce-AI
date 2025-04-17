package com.ptit.data.remote.util

import com.google.gson.annotations.SerializedName

internal data class BaseSuccessResponse<T>(
    @SerializedName("data")
    val `data`: T?,
    @SerializedName("statusCode")
    val statusCode: Int?
)