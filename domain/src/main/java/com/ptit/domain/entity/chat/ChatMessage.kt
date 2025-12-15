package com.ptit.domain.entity.chat

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val timestamp: Long,
    val tags: List<UiTag>,
    val isPending: Boolean
)

enum class ChatRole {
    USER,
    MODEL
}

/**
 * Simplified order item for display in chat
 */
data class ChatOrderItem(
    val id: String,
    val orderCode: String?,
    val status: String,
    val totalAmount: Int,
    val createdAt: String,
    val itemCount: Int
)

/**
 * Simplified product item for display in chat
 */
data class ChatProductItem(
    val id: String,
    val name: String,
    val basePrice: Int,
    val virtualPrice: Int?,
    val mainImage: String,
    val rating: Float,
    val sold: Int,
    val shopName: String?
) {
    val hasDiscount: Boolean
        get() = virtualPrice != null && virtualPrice > basePrice && virtualPrice > 0

    val discountPercent: Int
        get() = if (hasDiscount && virtualPrice != null) {
            ((virtualPrice.toFloat() - basePrice) * 100 / virtualPrice).toInt()
        } else {
            0
        }
}

/**
 * Detailed product for display in chat
 */
data class ChatProductDetail(
    val id: String,
    val name: String,
    val basePrice: Int,
    val virtualPrice: Int?,
    val images: List<String>,
    val description: String,
    val rating: Float,
    val sold: Int,
    val category: String?,
    val brand: String?,
    val shopName: String?,
    val shopId: String?,
    val variants: List<ChatVariant>,
    val skus: List<ChatSku>
) {
    val hasDiscount: Boolean
        get() = virtualPrice != null && virtualPrice > basePrice && virtualPrice > 0

    val discountPercent: Int
        get() = if (hasDiscount && virtualPrice != null) {
            ((virtualPrice.toFloat() - basePrice) * 100 / virtualPrice).toInt()
        } else {
            0
        }
}

data class ChatVariant(
    val name: String,
    val options: List<String>
)

data class ChatSku(
    val id: String,
    val value: String,
    val price: Int,
    val stock: Int,
    val image: String
)

/**
 * Cart item for display in chat
 */
data class ChatCartItem(
    val cartItemId: String,
    val productId: String?,
    val productName: String,
    val skuId: String,
    val skuValue: String?,
    val quantity: Int,
    val price: Int,
    val image: String,
    val shopName: String?
)

/**
 * Pagination info for product search
 */
data class ChatPaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val searchQuery: String?
)

sealed class UiTag {
    data object DisplayRecentOrders : UiTag()
    data object DisplayPaymentMethods : UiTag()
    data class NavigateToScreen(val screen: String) : UiTag()
    data class QuickReplies(val options: List<String>) : UiTag()
    data class DisplayOrderList(val orders: List<ChatOrderItem>) : UiTag()

    // Product-related tags
    data class DisplayProductList(
        val products: List<ChatProductItem>,
        val pagination: ChatPaginationInfo
    ) : UiTag()
    data class DisplayProductDetail(val product: ChatProductDetail) : UiTag()

    // Cart-related tags
    data class DisplayCart(
        val items: List<ChatCartItem>,
        val totalAmount: Int,
        val totalItems: Int
    ) : UiTag()
    data class AddToCartSuccess(val productName: String, val quantity: Int) : UiTag()
    data class RemoveFromCartSuccess(val productName: String) : UiTag()
}