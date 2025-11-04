package com.ptit.data.remote.dto.order

import com.google.gson.annotations.SerializedName

// 🧱 Product SKU Snapshot trong đơn hàng
data class ProductSKUSnapshotDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String?,
    @SerializedName("productName") val productName: String,
    @SerializedName("skuPrice") val skuPrice: Int,
    @SerializedName("image") val image: String,
    @SerializedName("skuValue") val skuValue: String,
    @SerializedName("skuId") val skuId: String?,
    @SerializedName("orderId") val orderId: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("createdAt") val createdAt: String
)

// 👤 Receiver (người nhận hàng)
data class ReceiverDto(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String
)

// 🧾 Đơn hàng
data class OrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("shopId") val shopId: String?,
    @SerializedName("status") val status: String,
    @SerializedName("totalAmount") val totalAmount: Int,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("receiver") val receiver: ReceiverDto?,
    @SerializedName("items") val items: List<ProductSKUSnapshotDto>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String?
)

// 📋 Danh sách đơn hàng (GET /orders)
data class GetOrderListResponseDto(
    @SerializedName("data") val data: List<OrderDto>,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

// 📦 Body tạo đơn hàng
data class CreateOrderRequestDto(
    @SerializedName("shopId") val shopId: String,
    @SerializedName("receiver") val receiver: ReceiverDto,
    @SerializedName("cartItemIds") val cartItemIds: List<String>
)

// ✅ Response tạo đơn hàng
data class CreateOrderResponseDto(
    @SerializedName("orders") val orders: List<OrderDto>,
    @SerializedName("paymentId") val paymentId: String
)

// ❌ Response khi hủy đơn hàng
data class CancelOrderResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String
)



