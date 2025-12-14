package com.ptit.data.remote.dto.file

import com.google.gson.annotations.SerializedName

// Single file item in response
data class UploadedFile(
    @SerializedName("url")
    val url: String
)

// Backend returns array of {url: string}
data class FileUploadResponse(
    @SerializedName("data")
    val data: List<UploadedFile>,

    @SerializedName("success")
    val success: Boolean = true
)