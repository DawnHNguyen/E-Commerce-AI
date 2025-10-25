package com.ptit.data.remote.dto.home

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.product.ProductDto

data class ListProductResponse(
    @SerializedName("data")  // Thay đổi từ "products" thành "data"
    val data: List<ProductDto>?,
    @SerializedName("metadata")
    val metadata: MetadataDto?
)

data class MetadataDto(
    @SerializedName("totalItems")
    val totalItems: Int?,
    @SerializedName("page")
    val page: Int?,
    @SerializedName("limit")
    val limit: Int?,
    @SerializedName("totalPages")
    val totalPages: Int?,
    @SerializedName("hasNext")
    val hasNext: Boolean?,
    @SerializedName("hasPrev")
    val hasPrev: Boolean?
)