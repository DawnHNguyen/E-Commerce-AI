package com.ptit.data.mapping

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import comptitdatabase.GetAllChatSessions
import comptitdatabase.TblChatMessage
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.ChatOrderItem
import com.ptit.domain.entity.chat.ChatSession
import com.ptit.domain.entity.chat.ChatRole
import com.ptit.domain.entity.chat.UiTag

data class UiTagDto(
    val type: String,
    val data: Map<String, Any> = emptyMap()
)

object ChatMapping {

    private val gson = Gson()

    fun GetAllChatSessions.toDomain(): ChatSession {
        return ChatSession(
            id = id,
            title = title,
            lastMessage = lastMessage ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun TblChatMessage.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            role = when (role) {
                "USER" -> ChatRole.USER
                "MODEL" -> ChatRole.MODEL
                else -> ChatRole.USER
            },
            content = content,
            timestamp = timestamp,
            tags = parseTagsFromString(tags),
            isPending = isPending == 1L
        )
    }

    fun ChatRole.toStringValue(): String {
        return when (this) {
            ChatRole.USER -> "USER"
            ChatRole.MODEL -> "MODEL"
        }
    }

    /**
     * Parse UI control tags from AI response text using regex patterns
     * Following the original prompt specification for tag formats
     */
    fun parseTagsFromText(text: String): List<UiTag> {
        val tags = mutableListOf<UiTag>()
        
        // Parse [DISPLAY_RECENT_ORDERS]
        if (text.contains("[DISPLAY_RECENT_ORDERS]")) {
            tags.add(UiTag.DisplayRecentOrders)
        }
        
        // Parse [DISPLAY_PAYMENT_METHODS]
        if (text.contains("[DISPLAY_PAYMENT_METHODS]")) {
            tags.add(UiTag.DisplayPaymentMethods)
        }
        
        // Parse [NAVIGATE_TO:screen_name]
        val navigateRegex = Regex("""\[NAVIGATE_TO:([^\]]+)\]""")
        navigateRegex.findAll(text).forEach { match ->
            val screenName = match.groupValues[1]
            tags.add(UiTag.NavigateToScreen(screenName))
        }
        
        // Parse [QUICK_REPLIES:option1|option2|option3]
        val quickRepliesRegex = Regex("""\[QUICK_REPLIES:([^\]]+)\]""")
        quickRepliesRegex.findAll(text).forEach { match ->
            val optionsString = match.groupValues[1]
            val options = optionsString.split("|").map { it.trim() }
            tags.add(UiTag.QuickReplies(options))
        }
        
        return tags
    }

    private fun parseTagsFromString(tagsJson: String): List<UiTag> {
        if (tagsJson.isEmpty()) return emptyList()

        return try {
            val type = object : TypeToken<List<UiTagDto>>() {}.type
            val tagDtos = gson.fromJson<List<UiTagDto>>(tagsJson, type)

            tagDtos.map { dto ->
                when (dto.type) {
                    "DISPLAY_RECENT_ORDERS" -> UiTag.DisplayRecentOrders
                    "DISPLAY_PAYMENT_METHODS" -> UiTag.DisplayPaymentMethods
                    "NAVIGATE_TO_SCREEN" -> {
                        val screen = dto.data["screen"] as? String ?: ""
                        UiTag.NavigateToScreen(screen)
                    }
                    "QUICK_REPLIES" -> {
                        @Suppress("UNCHECKED_CAST")
                        val options = dto.data["options"] as? List<String> ?: emptyList()
                        UiTag.QuickReplies(options)
                    }
                    "DISPLAY_ORDER_LIST" -> {
                        @Suppress("UNCHECKED_CAST")
                        val ordersData = dto.data["orders"] as? List<Map<String, Any>> ?: emptyList()
                        val orders = ordersData.map { orderMap ->
                            ChatOrderItem(
                                id = orderMap["id"] as? String ?: "",
                                orderCode = orderMap["orderCode"] as? String,
                                status = orderMap["status"] as? String ?: "",
                                totalAmount = (orderMap["totalAmount"] as? Number)?.toInt() ?: 0,
                                createdAt = orderMap["createdAt"] as? String ?: "",
                                itemCount = (orderMap["itemCount"] as? Number)?.toInt() ?: 0
                            )
                        }
                        UiTag.DisplayOrderList(orders)
                    }
                    else -> null
                }
            }.filterNotNull()
        } catch (e: JsonSyntaxException) {
            emptyList()
        }
    }

    fun serializeTagsToString(tags: List<UiTag>): String {
        if (tags.isEmpty()) return ""

        val tagDtos = tags.map { tag ->
            when (tag) {
                is UiTag.DisplayRecentOrders -> UiTagDto("DISPLAY_RECENT_ORDERS")
                is UiTag.DisplayPaymentMethods -> UiTagDto("DISPLAY_PAYMENT_METHODS")
                is UiTag.NavigateToScreen -> UiTagDto(
                    type = "NAVIGATE_TO_SCREEN",
                    data = mapOf("screen" to tag.screen)
                )
                is UiTag.QuickReplies -> UiTagDto(
                    type = "QUICK_REPLIES",
                    data = mapOf("options" to tag.options)
                )
                is UiTag.DisplayOrderList -> UiTagDto(
                    type = "DISPLAY_ORDER_LIST",
                    data = mapOf("orders" to tag.orders.map { order ->
                        mapOf(
                            "id" to order.id,
                            "orderCode" to order.orderCode,
                            "status" to order.status,
                            "totalAmount" to order.totalAmount,
                            "createdAt" to order.createdAt,
                            "itemCount" to order.itemCount
                        )
                    })
                )
            }
        }

        return try {
            gson.toJson(tagDtos)
        } catch (e: Exception) {
            ""
        }
    }
}