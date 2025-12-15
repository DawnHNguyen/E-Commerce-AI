package com.ptit.data.mapping

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import comptitdatabase.GetAllChatSessions
import comptitdatabase.TblChatMessage
import com.ptit.domain.entity.chat.ChatCartItem
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.ChatOrderItem
import com.ptit.domain.entity.chat.ChatPaginationInfo
import com.ptit.domain.entity.chat.ChatProductDetail
import com.ptit.domain.entity.chat.ChatProductItem
import com.ptit.domain.entity.chat.ChatSession
import com.ptit.domain.entity.chat.ChatRole
import com.ptit.domain.entity.chat.ChatSku
import com.ptit.domain.entity.chat.ChatVariant
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
                    "DISPLAY_PRODUCT_LIST" -> {
                        @Suppress("UNCHECKED_CAST")
                        val productsData = dto.data["products"] as? List<Map<String, Any>> ?: emptyList()
                        val products = productsData.map { productMap ->
                            ChatProductItem(
                                id = productMap["id"] as? String ?: "",
                                name = productMap["name"] as? String ?: "",
                                basePrice = (productMap["basePrice"] as? Number)?.toInt() ?: 0,
                                virtualPrice = (productMap["virtualPrice"] as? Number)?.toInt(),
                                mainImage = productMap["mainImage"] as? String ?: "",
                                rating = (productMap["rating"] as? Number)?.toFloat() ?: 0f,
                                sold = (productMap["sold"] as? Number)?.toInt() ?: 0,
                                shopName = productMap["shopName"] as? String
                            )
                        }
                        @Suppress("UNCHECKED_CAST")
                        val paginationMap = dto.data["pagination"] as? Map<String, Any> ?: emptyMap()
                        val pagination = ChatPaginationInfo(
                            currentPage = (paginationMap["currentPage"] as? Number)?.toInt() ?: 1,
                            totalPages = (paginationMap["totalPages"] as? Number)?.toInt() ?: 1,
                            totalItems = (paginationMap["totalItems"] as? Number)?.toInt() ?: 0,
                            hasNextPage = paginationMap["hasNextPage"] as? Boolean ?: false,
                            hasPreviousPage = paginationMap["hasPreviousPage"] as? Boolean ?: false,
                            searchQuery = paginationMap["searchQuery"] as? String
                        )
                        UiTag.DisplayProductList(products, pagination)
                    }
                    "DISPLAY_PRODUCT_DETAIL" -> {
                        @Suppress("UNCHECKED_CAST")
                        val productMap = dto.data["product"] as? Map<String, Any> ?: emptyMap()
                        @Suppress("UNCHECKED_CAST")
                        val variantsData = productMap["variants"] as? List<Map<String, Any>> ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        val skusData = productMap["skus"] as? List<Map<String, Any>> ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        val imagesData = productMap["images"] as? List<String> ?: emptyList()

                        val product = ChatProductDetail(
                            id = productMap["id"] as? String ?: "",
                            name = productMap["name"] as? String ?: "",
                            basePrice = (productMap["basePrice"] as? Number)?.toInt() ?: 0,
                            virtualPrice = (productMap["virtualPrice"] as? Number)?.toInt(),
                            images = imagesData,
                            description = productMap["description"] as? String ?: "",
                            rating = (productMap["rating"] as? Number)?.toFloat() ?: 0f,
                            sold = (productMap["sold"] as? Number)?.toInt() ?: 0,
                            category = productMap["category"] as? String,
                            brand = productMap["brand"] as? String,
                            shopName = productMap["shopName"] as? String,
                            shopId = productMap["shopId"] as? String,
                            variants = variantsData.map { variantMap ->
                                @Suppress("UNCHECKED_CAST")
                                ChatVariant(
                                    name = variantMap["name"] as? String ?: "",
                                    options = variantMap["options"] as? List<String> ?: emptyList()
                                )
                            },
                            skus = skusData.map { skuMap ->
                                ChatSku(
                                    id = skuMap["id"] as? String ?: "",
                                    value = skuMap["value"] as? String ?: "",
                                    price = (skuMap["price"] as? Number)?.toInt() ?: 0,
                                    stock = (skuMap["stock"] as? Number)?.toInt() ?: 0,
                                    image = skuMap["image"] as? String ?: ""
                                )
                            }
                        )
                        UiTag.DisplayProductDetail(product)
                    }
                    "DISPLAY_CART" -> {
                        @Suppress("UNCHECKED_CAST")
                        val itemsData = dto.data["items"] as? List<Map<String, Any>> ?: emptyList()
                        val items = itemsData.map { itemMap ->
                            ChatCartItem(
                                cartItemId = itemMap["cartItemId"] as? String ?: "",
                                productId = itemMap["productId"] as? String,
                                productName = itemMap["productName"] as? String ?: "",
                                skuId = itemMap["skuId"] as? String ?: "",
                                skuValue = itemMap["skuValue"] as? String,
                                quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0,
                                price = (itemMap["price"] as? Number)?.toInt() ?: 0,
                                image = itemMap["image"] as? String ?: "",
                                shopName = itemMap["shopName"] as? String
                            )
                        }
                        val totalAmount = (dto.data["totalAmount"] as? Number)?.toInt() ?: 0
                        val totalItems = (dto.data["totalItems"] as? Number)?.toInt() ?: 0
                        UiTag.DisplayCart(items, totalAmount, totalItems)
                    }
                    "ADD_TO_CART_SUCCESS" -> {
                        val productName = dto.data["productName"] as? String ?: ""
                        val quantity = (dto.data["quantity"] as? Number)?.toInt() ?: 1
                        UiTag.AddToCartSuccess(productName, quantity)
                    }
                    "REMOVE_FROM_CART_SUCCESS" -> {
                        val productName = dto.data["productName"] as? String ?: ""
                        UiTag.RemoveFromCartSuccess(productName)
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
                is UiTag.DisplayProductList -> UiTagDto(
                    type = "DISPLAY_PRODUCT_LIST",
                    data = mapOf(
                        "products" to tag.products.map { product ->
                            mapOf(
                                "id" to product.id,
                                "name" to product.name,
                                "basePrice" to product.basePrice,
                                "virtualPrice" to product.virtualPrice,
                                "mainImage" to product.mainImage,
                                "rating" to product.rating,
                                "sold" to product.sold,
                                "shopName" to product.shopName
                            )
                        },
                        "pagination" to mapOf(
                            "currentPage" to tag.pagination.currentPage,
                            "totalPages" to tag.pagination.totalPages,
                            "totalItems" to tag.pagination.totalItems,
                            "hasNextPage" to tag.pagination.hasNextPage,
                            "hasPreviousPage" to tag.pagination.hasPreviousPage,
                            "searchQuery" to tag.pagination.searchQuery
                        )
                    )
                )
                is UiTag.DisplayProductDetail -> UiTagDto(
                    type = "DISPLAY_PRODUCT_DETAIL",
                    data = mapOf(
                        "product" to mapOf(
                            "id" to tag.product.id,
                            "name" to tag.product.name,
                            "basePrice" to tag.product.basePrice,
                            "virtualPrice" to tag.product.virtualPrice,
                            "images" to tag.product.images,
                            "description" to tag.product.description,
                            "rating" to tag.product.rating,
                            "sold" to tag.product.sold,
                            "category" to tag.product.category,
                            "brand" to tag.product.brand,
                            "shopName" to tag.product.shopName,
                            "shopId" to tag.product.shopId,
                            "variants" to tag.product.variants.map { variant ->
                                mapOf(
                                    "name" to variant.name,
                                    "options" to variant.options
                                )
                            },
                            "skus" to tag.product.skus.map { sku ->
                                mapOf(
                                    "id" to sku.id,
                                    "value" to sku.value,
                                    "price" to sku.price,
                                    "stock" to sku.stock,
                                    "image" to sku.image
                                )
                            }
                        )
                    )
                )
                is UiTag.DisplayCart -> UiTagDto(
                    type = "DISPLAY_CART",
                    data = mapOf(
                        "items" to tag.items.map { item ->
                            mapOf(
                                "cartItemId" to item.cartItemId,
                                "productId" to item.productId,
                                "productName" to item.productName,
                                "skuId" to item.skuId,
                                "skuValue" to item.skuValue,
                                "quantity" to item.quantity,
                                "price" to item.price,
                                "image" to item.image,
                                "shopName" to item.shopName
                            )
                        },
                        "totalAmount" to tag.totalAmount,
                        "totalItems" to tag.totalItems
                    )
                )
                is UiTag.AddToCartSuccess -> UiTagDto(
                    type = "ADD_TO_CART_SUCCESS",
                    data = mapOf(
                        "productName" to tag.productName,
                        "quantity" to tag.quantity
                    )
                )
                is UiTag.RemoveFromCartSuccess -> UiTagDto(
                    type = "REMOVE_FROM_CART_SUCCESS",
                    data = mapOf("productName" to tag.productName)
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