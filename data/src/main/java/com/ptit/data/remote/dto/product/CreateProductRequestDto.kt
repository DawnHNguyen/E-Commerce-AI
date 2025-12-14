package com.ptit.data.remote.dto.product

import com.google.gson.annotations.SerializedName

data class SpecificationDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: String
)

data class CreateProductRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("publishedAt")
    val publishedAt: String?,
    @SerializedName("basePrice")
    val basePrice: Int,
    @SerializedName("virtualPrice")
    val virtualPrice: Int?,
    @SerializedName("brandId")
    val brandId: String?,
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("images")
    val images: List<String>,
    @SerializedName("variants")
    val variants: List<VariantDto>,
    @SerializedName("skus")
    val skus: List<SKUDto>,
    @SerializedName("specifications")
    val specifications: List<SpecificationDto> = emptyList()
)
