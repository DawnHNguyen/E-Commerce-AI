package com.ptit.data.repository

import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.ptit.data.local.datasource.ChatLocalDataSource
import com.ptit.data.mapping.ChatMapping
import com.ptit.data.mapping.ChatMapping.toDomain
import com.ptit.data.remote.datasource.GeminiRemoteDataSource
import com.ptit.domain.entity.cart.DeleteCartRequestDomainEntity
import com.ptit.domain.entity.chat.ChatCartItem
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.ChatOrderItem
import com.ptit.domain.entity.chat.ChatPaginationInfo
import com.ptit.domain.entity.chat.ChatProductDetail
import com.ptit.domain.entity.chat.ChatProductItem
import com.ptit.domain.entity.chat.ChatRole
import com.ptit.domain.entity.chat.ChatSession
import com.ptit.domain.entity.chat.ChatSku
import com.ptit.domain.entity.chat.ChatVariant
import com.ptit.domain.entity.chat.UiTag
import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.domain.repository.CartRepository
import com.ptit.domain.repository.ChatRepository
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val geminiDataSource: GeminiRemoteDataSource,
    private val orderRepository: OrderRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val productRepository: ProductRepository,
    private val productRemoteDataSource: ProductRemoteDataSource,
    private val cartRepository: CartRepository
) : ChatRepository {

    private val gson = Gson()

    // Store last search context for pagination
    private var lastSearchQuery: String? = null
    private var lastSearchLimit: Int = 5

    // Session management
    override fun getAllSessions(): Flow<List<ChatSession>> {
        return localDataSource.getAllSessionsFlow().map { sessions ->
            sessions.map { it.toDomain() }
        }
    }

    override suspend fun createSession(title: String): Resource<ChatSession> = withContext(Dispatchers.IO) {
        try {
            val sessionId = UUID.randomUUID().toString()
            localDataSource.insertChatSession(sessionId, title)
            val timestamp = System.currentTimeMillis()
            Resource.success(ChatSession(
                id = sessionId,
                title = title,
                lastMessage = "",
                createdAt = timestamp,
                updatedAt = timestamp
            ))
        } catch (e: Exception) {
            Resource.error(UnknownException(
                message = e.message ?: "Failed to create session",
                requestUrl = "",
                error = null
            ))
        }
    }

    override suspend fun deleteSession(sessionId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            localDataSource.deleteSession(sessionId)
            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(UnknownException(
                message = e.message ?: "Failed to delete session",
                requestUrl = "",
                error = null
            ))
        }
    }

    // Message operations
    override suspend fun sendMessage(sessionId: String, messageContent: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.loading())

            // 1. Save user message locally immediately
            localDataSource.insertChatMessage(
                sessionId = sessionId,
                role = ChatRole.USER.toString(),
                content = messageContent,
                isPending = false
            )

            // 2. Start chat session if not started or reload with history
            val dbMessages = localDataSource.getAllChatMessages(sessionId)
            val chatHistory = dbMessages.map { dbMessage ->
                content(dbMessage.role.lowercase()) {
                    text(dbMessage.content)
                }
            }
            geminiDataSource.startChatSession(chatHistory)

            // 3. Send to AI and handle response loop
            val aiResponse = geminiDataSource.sendMessage(messageContent)
            handleAIResponse(aiResponse, sessionId)

            emit(Resource.success(Unit))

        } catch (e: Exception) {
            emit(Resource.error(
                UnknownException(
                    message = e.message ?: "Unknown error occurred",
                    requestUrl = "",
                    error = null
                )
            ))
        }
    }

    override fun getChatHistory(sessionId: String): Flow<List<ChatMessage>> {
        return localDataSource.getAllChatMessagesFlow(sessionId).map { dbMessages ->
            dbMessages.map { it.toDomain() }
        }
    }

    override suspend fun clearChatHistory(sessionId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            localDataSource.deleteChatHistory(sessionId)
            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(
                UnknownException(
                    message = e.message ?: "Failed to clear chat history",
                    requestUrl = "",
                    error = null
                )
            )
        }
    }

    private suspend fun handleAIResponse(response: GenerateContentResponse, sessionId: String) {
        var currentResponse = response
        var maxRetries = 3
        var retryCount = 0

        // Function calling loop using text-based parsing
        while (retryCount < maxRetries) {
            val textContent = currentResponse.text ?: ""

            // Check if response contains a function call request
            val functionCallData = geminiDataSource.parseFunctionCall(textContent)

            if (functionCallData != null) {
                // Handle navigation functions specially - save message with navigation tag immediately
                if (functionCallData.name == "navigate_to_add_payment_screen") {
                    val tags = listOf(UiTag.NavigateToScreen("add_payment"))
                    val tagsJson = ChatMapping.serializeTagsToString(tags)

                    localDataSource.insertChatMessage(
                        sessionId = sessionId,
                        role = ChatRole.MODEL.toString(),
                        content = "Đang chuyển đến màn hình thêm phương thức thanh toán...",
                        tags = tagsJson,
                        isPending = false
                    )
                    return
                }

                // Handle list_orders function - fetch orders and show selection UI
                if (functionCallData.name == "list_orders") {
                    val ordersResult = fetchOrdersForSelection()
                    if (ordersResult != null) {
                        val tags = listOf(UiTag.DisplayOrderList(ordersResult))
                        val tagsJson = ChatMapping.serializeTagsToString(tags)

                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = "Vui lòng chọn đơn hàng bạn muốn xem:",
                            tags = tagsJson,
                            isPending = false
                        )
                    } else {
                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = "Bạn chưa có đơn hàng nào.",
                            tags = "",
                            isPending = false
                        )
                    }
                    return
                }

                // Handle search_products function - fetch and display product list with pagination UI
                if (functionCallData.name == "search_products") {
                    val searchResult = searchProducts(functionCallData.params)
                    if (searchResult != null) {
                        val tags = listOf(UiTag.DisplayProductList(searchResult.first, searchResult.second))
                        val tagsJson = ChatMapping.serializeTagsToString(tags)

                        val pagination = searchResult.second
                        val searchQuery = pagination.searchQuery
                        val message = buildString {
                            append("Tìm thấy ${pagination.totalItems} sản phẩm")
                            if (!searchQuery.isNullOrEmpty()) {
                                append(" cho \"${searchQuery.replace("_", " ")}\"")
                            }
                            append(" (Trang ${pagination.currentPage}/${pagination.totalPages}):")
                        }

                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = message,
                            tags = tagsJson,
                            isPending = false
                        )
                    } else {
                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = "Không tìm thấy sản phẩm nào phù hợp.",
                            tags = "",
                            isPending = false
                        )
                    }
                    return
                }

                // Handle view_product_detail function - fetch and display product detail
                if (functionCallData.name == "view_product_detail") {
                    val productDetail = fetchProductDetail(functionCallData.paramValue)
                    if (productDetail != null) {
                        val tags = listOf(UiTag.DisplayProductDetail(productDetail))
                        val tagsJson = ChatMapping.serializeTagsToString(tags)

                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = "Chi tiết sản phẩm:",
                            tags = tagsJson,
                            isPending = false
                        )
                    } else {
                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = "Không tìm thấy sản phẩm này.",
                            tags = "",
                            isPending = false
                        )
                    }
                    return
                }

                // Handle get_cart function - fetch and display cart
                if (functionCallData.name == "get_cart") {
                    val cartResult = fetchCart()
                    if (cartResult != null) {
                        val tags = listOf(UiTag.DisplayCart(cartResult.first, cartResult.second, cartResult.third))
                        val tagsJson = ChatMapping.serializeTagsToString(tags)

                        val message = if (cartResult.first.isEmpty()) {
                            "Giỏ hàng của bạn đang trống."
                        } else {
                            "Giỏ hàng của bạn (${cartResult.third} sản phẩm):"
                        }

                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = message,
                            tags = tagsJson,
                            isPending = false
                        )
                    } else {
                        localDataSource.insertChatMessage(
                            sessionId = sessionId,
                            role = ChatRole.MODEL.toString(),
                            content = "Không thể tải giỏ hàng. Vui lòng thử lại sau.",
                            tags = "",
                            isPending = false
                        )
                    }
                    return
                }

                // Execute the function call
                val functionResult = executeFunctionCall(functionCallData)

                // Send function result back to AI
                val resultPrompt = "Function ${functionCallData.name} result: $functionResult. Please provide a human-readable response in Vietnamese."
                currentResponse = geminiDataSource.sendMessage(resultPrompt)
                retryCount++
            } else {
                // No function call - process as final response

                // Parse UI tags from response
                val parsedTags = ChatMapping.parseTagsFromText(textContent)
                val tagsJson = ChatMapping.serializeTagsToString(parsedTags)

                // Save AI response to local database
                localDataSource.insertChatMessage(
                    sessionId = sessionId,
                    role = ChatRole.MODEL.toString(),
                    content = textContent,
                    tags = tagsJson,
                    isPending = false
                )

                break
            }
        }

        // If we hit max retries, save whatever response we have
        if (retryCount >= maxRetries) {
            val textContent = currentResponse.text ?: "Đã xảy ra lỗi trong quá trình xử lý."
            val parsedTags = ChatMapping.parseTagsFromText(textContent)
            val tagsJson = ChatMapping.serializeTagsToString(parsedTags)

            localDataSource.insertChatMessage(
                sessionId = sessionId,
                role = ChatRole.MODEL.toString(),
                content = textContent,
                tags = tagsJson,
                isPending = false
            )
        }
    }

    private suspend fun executeFunctionCall(functionCallData: GeminiRemoteDataSource.FunctionCallData): String {
        return try {
            when (functionCallData.name) {
                "check_order_status" -> {
                    val orderId = functionCallData.paramValue
                    checkOrderStatus(orderId)
                }
                "list_payment_methods" -> {
                    listPaymentMethods()
                }
                "delete_payment_method" -> {
                    val last4 = functionCallData.paramValue
                    deletePaymentMethod(last4)
                }
                "navigate_to_add_payment_screen" -> {
                    navigateToAddPaymentScreen()
                }
                "add_to_cart" -> {
                    val skuId = functionCallData.params.getOrNull(0) ?: ""
                    val quantity = functionCallData.params.getOrNull(1)?.toIntOrNull() ?: 1
                    addToCart(skuId, quantity)
                }
                "remove_from_cart" -> {
                    val cartItemId = functionCallData.paramValue
                    removeFromCart(cartItemId)
                }
                "update_cart_quantity" -> {
                    val cartItemId = functionCallData.params.getOrNull(0) ?: ""
                    val skuId = functionCallData.params.getOrNull(1) ?: ""
                    val quantity = functionCallData.params.getOrNull(2)?.toIntOrNull() ?: 1
                    updateCartQuantity(cartItemId, skuId, quantity)
                }
                else -> {
                    gson.toJson(mapOf("error" to "Unknown function: ${functionCallData.name}"))
                }
            }
        } catch (e: Exception) {
            gson.toJson(mapOf("error" to (e.message ?: "Function execution failed")))
        }
    }

    private suspend fun checkOrderStatus(orderId: String): String = withContext(Dispatchers.IO) {
        when (val result = orderRepository.getOrderById(orderId)) {
            is Resource.Success -> {
                val order = result.data

                // Map order items to detailed product info
                val itemsInfo = order.items?.map { item ->
                    mapOf(
                        "productName" to item.productName,
                        "skuValue" to item.skuValue,
                        "quantity" to item.quantity,
                        "price" to item.skuPrice,
                        "subtotal" to (item.skuPrice * item.quantity)
                    )
                } ?: emptyList()

                // Map status to Vietnamese
                val statusVietnamese = when (order.status.lowercase()) {
                    "pending" -> "Chờ xử lý"
                    "confirmed" -> "Đã xác nhận"
                    "shipping" -> "Đang giao hàng"
                    "delivered" -> "Đã giao hàng"
                    "cancelled" -> "Đã hủy"
                    else -> order.status
                }

                gson.toJson(mapOf(
                    "success" to true,
                    "order" to mapOf(
                        "id" to order.id,
                        "orderCode" to order.orderCode,
                        "status" to order.status,
                        "statusVietnamese" to statusVietnamese,
                        "paymentMethod" to order.paymentMethod,
                        "items" to itemsInfo,
                        "itemCount" to (order.items?.size ?: 0),
                        "totalItemCost" to order.totalItemCost,
                        "totalShippingFee" to order.totalShippingFee,
                        "totalVoucherDiscount" to order.totalVoucherDiscount,
                        "totalPayment" to order.totalPayment,
                        "receiverName" to order.receiver?.name,
                        "receiverPhone" to order.receiver?.phone,
                        "receiverAddress" to order.receiver?.address,
                        "createdAt" to order.createdAt,
                        "updatedAt" to order.updatedAt
                    )
                ))
            }
            is Resource.Error -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Không thể tìm thấy đơn hàng với mã: $orderId"
                ))
            }
            else -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Đang kiểm tra đơn hàng..."
                ))
            }
        }
    }

    private suspend fun listPaymentMethods(): String = withContext(Dispatchers.IO) {
        try {
            // Get payment methods from Flow - take first emission
            val methods = paymentMethodRepository.getPaymentMethods().first()
            val paymentMethods = methods.map { method ->
                mapOf(
                    "lastFour" to method.lastFourNum,
                    "cardType" to method.cardType.name,
                    "isDefault" to method.isDefault
                )
            }
            
            gson.toJson(mapOf(
                "success" to true,
                "paymentMethods" to paymentMethods
            ))
        } catch (e: Exception) {
            gson.toJson(mapOf(
                "success" to false,
                "error" to "Không thể lấy danh sách phương thức thanh toán"
            ))
        }
    }

    private suspend fun deletePaymentMethod(last4: String): String = withContext(Dispatchers.IO) {
        try {
            // Find payment method by last4 digits - use first emission
            val methods = paymentMethodRepository.getPaymentMethods().first()
            val targetMethod = methods.firstOrNull { it.lastFourNum == last4 }
            
            when (targetMethod) {
                null -> {
                    gson.toJson(mapOf(
                        "success" to false,
                        "error" to "Không tìm thấy phương thức thanh toán với 4 số cuối: $last4"
                    ))
                }
                else -> {
                    val result = paymentMethodRepository.deletePaymentMethod(
                        firstSixNum = targetMethod.firstSixNum,
                        lastFourNum = targetMethod.lastFourNum
                    )
                    
                    when (result) {
                        is Resource.Success -> {
                            gson.toJson(mapOf(
                                "success" to true,
                                "message" to "Đã xóa phương thức thanh toán thành công"
                            ))
                        }
                        is Resource.Error -> {
                            gson.toJson(mapOf(
                                "success" to false,
                                "error" to "Không thể xóa phương thức thanh toán"
                            ))
                        }
                        else -> {
                            gson.toJson(mapOf(
                                "success" to false,
                                "error" to "Đang xử lý..."
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            gson.toJson(mapOf(
                "success" to false,
                "error" to "Lỗi khi xóa phương thức thanh toán: ${e.message}"
            ))
        }
    }

    private fun navigateToAddPaymentScreen(): String {
        return gson.toJson(mapOf(
            "success" to true,
            "action" to "navigate_to_add_payment",
            "message" to "Điều hướng đến màn hình thêm phương thức thanh toán"
        ))
    }

    private suspend fun fetchOrdersForSelection(): List<ChatOrderItem>? = withContext(Dispatchers.IO) {
        when (val result = orderRepository.getOrders(page = 1, limit = 10)) {
            is Resource.Success -> {
                val orders = result.data.data
                if (orders.isEmpty()) {
                    null
                } else {
                    orders.map { order ->
                        ChatOrderItem(
                            id = order.id,
                            orderCode = order.orderCode,
                            status = order.status,
                            totalAmount = order.totalAmount,
                            createdAt = order.createdAt,
                            itemCount = order.items?.size ?: 0
                        )
                    }
                }
            }
            else -> null
        }
    }

    // ==================== PRODUCT FUNCTIONS ====================

    private suspend fun searchProducts(params: List<String>): Pair<List<ChatProductItem>, ChatPaginationInfo>? = withContext(Dispatchers.IO) {
        val keyword = params.getOrNull(0)?.replace("_", " ") ?: ""
        val page = params.getOrNull(1)?.toIntOrNull() ?: 1
        val limit = params.getOrNull(2)?.toIntOrNull()?.coerceIn(1, 20) ?: 5

        // Store for pagination context
        lastSearchQuery = keyword
        lastSearchLimit = limit

        // Use remote data source directly to get full pagination info
        when (val result = productRemoteDataSource.listProducts(
            page = page,
            limit = limit,
            sortBy = null,
            orderBy = null,
            minPrice = null,
            maxPrice = null,
            name = keyword.ifEmpty { null },
            categories = null,
            brandIds = null
        )) {
            is Resource.Success -> {
                val response = result.data
                val products = response.data ?: emptyList()
                val metadata = response.metadata

                if (products.isEmpty() && page == 1) {
                    null
                } else {
                    val chatProducts = products.map { productDto ->
                        val product = productDto.toDomainEntity()
                        ChatProductItem(
                            id = product.id,
                            name = product.name,
                            basePrice = product.basePrice,
                            virtualPrice = product.virtualPrice,
                            mainImage = product.images.firstOrNull() ?: "",
                            rating = product.rating,
                            sold = product.sold,
                            shopName = product.shop?.name
                        )
                    }

                    // Use actual pagination info from API response
                    val pagination = ChatPaginationInfo(
                        currentPage = metadata?.page ?: page,
                        totalPages = metadata?.totalPages ?: 1,
                        totalItems = metadata?.totalItems ?: products.size,
                        hasNextPage = metadata?.hasNext ?: false,
                        hasPreviousPage = metadata?.hasPrev ?: (page > 1),
                        searchQuery = keyword.ifEmpty { null }
                    )

                    Pair(chatProducts, pagination)
                }
            }
            else -> null
        }
    }

    private suspend fun fetchProductDetail(productId: String): ChatProductDetail? = withContext(Dispatchers.IO) {
        when (val result = productRepository.getProductDetail(productId)) {
            is Resource.Success -> {
                val product = result.data
                ChatProductDetail(
                    id = product.id,
                    name = product.name,
                    basePrice = product.basePrice,
                    virtualPrice = product.virtualPrice,
                    images = product.images,
                    description = product.description,
                    rating = product.rating,
                    sold = product.sold,
                    category = product.category?.name,
                    brand = product.brand?.name,
                    shopName = product.shop?.name,
                    shopId = product.createdById,
                    variants = product.variants.map { variant ->
                        ChatVariant(
                            name = variant.name,
                            options = variant.options
                        )
                    },
                    skus = product.skus.map { sku ->
                        ChatSku(
                            id = sku.id,
                            value = sku.value,
                            price = sku.price,
                            stock = sku.stock,
                            image = sku.image
                        )
                    }
                )
            }
            else -> null
        }
    }

    // ==================== CART FUNCTIONS ====================

    private suspend fun fetchCart(): Triple<List<ChatCartItem>, Int, Int>? = withContext(Dispatchers.IO) {
        when (val result = cartRepository.getCart(page = 1, limit = 50)) {
            is Resource.Success -> {
                val cartData = result.data
                val items = mutableListOf<ChatCartItem>()
                var totalAmount = 0

                cartData.data.forEach { shopGroup ->
                    shopGroup.cartItems.forEach { cartItem ->
                        val sku = cartItem.sku
                        val product = sku?.product
                        val itemTotal = (sku?.price ?: 0) * cartItem.quantity

                        items.add(ChatCartItem(
                            cartItemId = cartItem.id,
                            productId = product?.id,
                            productName = product?.name ?: "Sản phẩm không xác định",
                            skuId = cartItem.skuId,
                            skuValue = sku?.value,
                            quantity = cartItem.quantity,
                            price = sku?.price ?: 0,
                            image = sku?.image ?: product?.mainImage ?: "",
                            shopName = shopGroup.shopName
                        ))

                        totalAmount += itemTotal
                    }
                }

                Triple(items, totalAmount, cartData.totalItems)
            }
            else -> null
        }
    }

    private suspend fun addToCart(skuId: String, quantity: Int): String = withContext(Dispatchers.IO) {
        if (skuId.isEmpty()) {
            return@withContext gson.toJson(mapOf(
                "success" to false,
                "error" to "Vui lòng chọn phân loại sản phẩm trước khi thêm vào giỏ hàng"
            ))
        }

        when (val result = cartRepository.addToCart(skuId, quantity)) {
            is Resource.Success -> {
                gson.toJson(mapOf(
                    "success" to true,
                    "message" to "Đã thêm $quantity sản phẩm vào giỏ hàng"
                ))
            }
            is Resource.Error -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Không thể thêm sản phẩm vào giỏ hàng: ${result.error.message}"
                ))
            }
            else -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Đang xử lý..."
                ))
            }
        }
    }

    private suspend fun removeFromCart(cartItemId: String): String = withContext(Dispatchers.IO) {
        if (cartItemId.isEmpty()) {
            return@withContext gson.toJson(mapOf(
                "success" to false,
                "error" to "Không tìm thấy sản phẩm trong giỏ hàng"
            ))
        }

        when (val result = cartRepository.deleteCartItems(DeleteCartRequestDomainEntity(listOf(cartItemId)))) {
            is Resource.Success -> {
                gson.toJson(mapOf(
                    "success" to true,
                    "message" to "Đã xóa sản phẩm khỏi giỏ hàng",
                    "deletedCount" to result.data.deletedCount
                ))
            }
            is Resource.Error -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Không thể xóa sản phẩm khỏi giỏ hàng"
                ))
            }
            else -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Đang xử lý..."
                ))
            }
        }
    }

    private suspend fun updateCartQuantity(cartItemId: String, skuId: String, quantity: Int): String = withContext(Dispatchers.IO) {
        if (cartItemId.isEmpty() || skuId.isEmpty()) {
            return@withContext gson.toJson(mapOf(
                "success" to false,
                "error" to "Thông tin sản phẩm không hợp lệ"
            ))
        }

        if (quantity <= 0) {
            // If quantity is 0 or less, remove from cart
            return@withContext removeFromCart(cartItemId)
        }

        when (val result = cartRepository.updateCartItem(cartItemId, skuId, quantity)) {
            is Resource.Success -> {
                gson.toJson(mapOf(
                    "success" to true,
                    "message" to "Đã cập nhật số lượng thành $quantity"
                ))
            }
            is Resource.Error -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Không thể cập nhật số lượng"
                ))
            }
            else -> {
                gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Đang xử lý..."
                ))
            }
        }
    }
}