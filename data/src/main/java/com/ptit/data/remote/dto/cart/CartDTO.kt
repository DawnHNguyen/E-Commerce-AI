package com.ptit.data.remote.dto.cart

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.common.UserDto
data class ProductCartDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<String>,
    @SerializedName("basePrice") val basePrice: Int,
    @SerializedName("virtualPrice") val virtualPrice: Int?
)

data class SKUCartDto(
    @SerializedName("id") val id: String,
    @SerializedName("image") val image: String?,
    @SerializedName("price") val price: Int,
    @SerializedName("stock") val stock: Int,
    @SerializedName("value") val value: String?,
    @SerializedName("product") val product: ProductCartDto?
)

data class CartItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("skuId") val skuId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("sku") val sku: SKUCartDto?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class CartItemDetailDto(
    @SerializedName("shop") val shop: UserDto,
    @SerializedName("cartItems") val cartItems: List<CartItemDto>
)

data class GetCartResponseDto(
    @SerializedName("data") val data: List<CartItemDetailDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class AddToCartRequestDto(
    @SerializedName("skuId") val skuId: String,
    @SerializedName("quantity") val quantity: Int
)

data class UpdateCartItemRequestDto(
    @SerializedName("skuId") val skuId: String,
    @SerializedName("quantity") val quantity: Int
)

data class DeleteCartRequestDto(
    @SerializedName("cartItemIds") val cartItemIds: List<String>
)

data class DeleteCartResponseDto(
    @SerializedName("deletedCount") val deletedCount: Int
)