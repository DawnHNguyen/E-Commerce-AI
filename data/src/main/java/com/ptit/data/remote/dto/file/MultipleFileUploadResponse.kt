package com.ptit.data.remote.dto.file

import com.google.gson.annotations.SerializedName

// Backend returns same format as single upload - array of {url: string}
data class MultipleFileUploadResponse (
    @SerializedName("data")
    val data: List<UploadedFile>,

    @SerializedName("success")
    val success: Boolean = true
)