package com.ptit.data.remote.util

import com.google.gson.annotations.SerializedName

data class BaseErrorResponse(
    @SerializedName("error")
    val error: Error?,
    @SerializedName("statusCode")
    val statusCode: Int?
) {
    data class Error(
        @SerializedName("message")
        val message: String?
    )
}