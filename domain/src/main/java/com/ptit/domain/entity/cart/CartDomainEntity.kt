package com.ptit.domain.entity.cart

data class ProductCartDomainEntity(
    val id: String,
    val name: String,
    val images: List<String> = emptyList(),
    val basePrice: Int,
    val virtualPrice: Int?
) {
    val mainImage: String
        get() = images.firstOrNull().orEmpty()
}

data class SKUCartDomainEntity(
    val id: String,
    val image: String?,
    val price: Int,
    val stock: Int,
    val value: String?,
    val product: ProductCartDomainEntity?
)

data class CartItemDomainEntity(
    val id: String,
    val quantity: Int,
    val skuId: String,
    val userId: String,
    val sku: SKUCartDomainEntity?,
    val createdAt: String,
    val updatedAt: String
)

data class CartItemDetailDomainEntity(
    val shopId: String?,
    val shopName: String?,
    val shopAvatar: String?,
    val cartItems: List<CartItemDomainEntity>
)

data class GetCartDomainEntity(
    val data: List<CartItemDetailDomainEntity>,
    val totalItems: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class DeleteCartRequestDomainEntity(
    val cartItemIds: List<String>
)

data class DeleteCartResponseDomainEntity(
    val deletedCount: Int
)
