package com.ptit.data.remote.dto.product

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.common.UserDto

data class ProductDto(
    @SerializedName("id") // changed from "_id" to "id"
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("basePrice")
    val basePrice: Int?,
    @SerializedName("virtualPrice")
    val virtualPrice: Int?,
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("variants")
    val variants: List<VariantDto>?,
    @SerializedName("skus")
    val skus: List<SKUDto>?,
    @SerializedName("category")
    val category: CategoryDto?,
    @SerializedName("brand")
    val brand: BrandDto?,
    @SerializedName("shopInfo")
    val shopInfo: ShopInfoDto?,
    @SerializedName("createdById")
    val createdById: String?,
    @SerializedName("isPublic")
    val isPublic: Boolean?,
    @SerializedName("publishedAt")
    val publishedAt: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,

    // Calculated fields
    @SerializedName("sold")
    val sold: Int?,
    @SerializedName("rating")
    val rating: Float?,
    @SerializedName("view")
    val view: Int?
)

data class BrandDto(
    @SerializedName("id") // changed from "_id" to "id"
    val id: String?,
    @SerializedName("name")
    val name: String?
)

data class SKUDto(
    @SerializedName("id")
    val id: String?, // added id field
    @SerializedName("value")
    val value: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("image")
    val image: String
)

data class VariantDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("options")
    val options: List<String>
)

data class ShopInfoDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("productsCount") val productsCount: Int?
)