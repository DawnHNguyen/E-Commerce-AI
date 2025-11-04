package com.ptit.domain.entity.order

data class ProductSKUSnapshotDomainEntity(
    val id: String,
    val productId: String?,
    val productName: String,
    val skuPrice: Int,
    val image: String,
    val skuValue: String,
    val skuId: String?,
    val orderId: String?,
    val quantity: Int,
    val createdAt: String
)

data class ReceiverDomainEntity(
    val name: String,
    val phone: String,
    val address: String
)

data class OrderDomainEntity(
    val id: String,
    val userId: String,
    val shopId: String,
    val status: String,
    val totalAmount: Int,
    val paymentMethod: String,
    val receiver: ReceiverDomainEntity?,
    val items: List<ProductSKUSnapshotDomainEntity>,
    val createdAt: String,
    val updatedAt: String?
)

// -----------------------------
// 📦 REQUEST / RESPONSE ENTITY
// -----------------------------

data class CreateOrderRequestDomainEntity(
    val shopId: String,
    val receiver: ReceiverDomainEntity,
    val cartItemIds: List<String>
)

data class CreateOrderResponseDomainEntity(
    val orders: List<OrderDomainEntity>,
    val paymentId: String
)

data class CancelOrderResponseDomainEntity(
    val id: String,
    val status: String
)

// -----------------------------
// 📃 PAGINATION ENTITY
// -----------------------------

data class GetOrderListDomainEntity(
    val data: List<OrderDomainEntity>,
    val totalItems: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)