package com.ptit.data.remote.dto.file

data class MultipleFileUploadResponse (
    val data: List<String>,
    val success: Boolean = true
)