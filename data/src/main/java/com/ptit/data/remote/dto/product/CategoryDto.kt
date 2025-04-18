package com.ptit.data.remote.dto.product

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
)
