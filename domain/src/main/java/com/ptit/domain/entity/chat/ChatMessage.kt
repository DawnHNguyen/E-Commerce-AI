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

sealed class UiTag {
    data object DisplayRecentOrders : UiTag()
    data object DisplayPaymentMethods : UiTag()
    data class NavigateToScreen(val screen: String) : UiTag()
    data class QuickReplies(val options: List<String>) : UiTag()
    data class DisplayOrderList(val orders: List<ChatOrderItem>) : UiTag()
}