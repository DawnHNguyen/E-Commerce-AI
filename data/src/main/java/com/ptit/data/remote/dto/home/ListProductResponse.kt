package com.ptit.data.remote.dto.home

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.product.ProductDto

data class ListProductResponse(
    @SerializedName("products")
    val products: List<ProductDto>?,
)