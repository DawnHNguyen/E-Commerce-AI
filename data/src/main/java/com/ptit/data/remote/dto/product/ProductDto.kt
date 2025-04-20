package com.ptit.data.remote.dto.product

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.common.UserDto

data class ProductDto(
    @SerializedName("category")
    val category: CategoryDto?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("price")
    val price: Int?,
    @SerializedName("price_before_discount")
    val priceBeforeDiscount: Int?,
    @SerializedName("quantity")
    val quantity: Int?,
    @SerializedName("rating")
    val rating: Float?,
    @SerializedName("shop")
    val shop: UserDto?,
    @SerializedName("sold")
    val sold: Int?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("view")
    val view: Int?,
    @SerializedName("description")
    var description: String? = "", // Add default value to avoid nullability issues
)
