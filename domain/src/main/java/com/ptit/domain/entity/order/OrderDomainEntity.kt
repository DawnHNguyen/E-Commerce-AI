package com.ptit.domain.entity.order

data class ProductSKUSnapshotDomainEntity(
    val id: String,
    val productId: String?,
    val productName: String,
    val skuPrice: Int, // Giá bán (sau giảm)
    val originalPrice: Int?, // Giá gốc (trước giảm)
    val image: String,
    val skuValue: String,
    val skuId: String?,
    val orderId: String?,
    val quantity: Int,
    val createdAt: String
)

// 🔴 SỬA: Thêm trường GHN
data class ReceiverDomainEntity(
    val name: String,
    val phone: String,
    val address: String,
    val provinceId: Int?,
    val districtId: Int?,
    val wardCode: String?
)

data class OrderDomainEntity(
    val id: String,
    val userId: String,
    val shopId: String,
    val status: String,
    val totalAmount: Int,
    val paymentMethod: String,
    val receiver: ReceiverDomainEntity?,
    val items: List<ProductSKUSnapshotDomainEntity>?,
    val createdAt: String,
    val updatedAt: String?,
    val orderCode: String?, // Mới

    val totalItemCost: Int,
    val totalShippingFee: Int,
    val totalVoucherDiscount: Int,
    val totalPayment: Int
)

// -----------------------------
// 📦 REQUEST / RESPONSE ENTITY
// -----------------------------
data class ShippingInfoDomainEntity(
    val serviceId: Int?,
    val serviceTypeId: Int?,
    val configFeeId: String?,
    val extraCostId: String?,
    val weight: Double,
    val length: Double,
    val width: Double,
    val height: Double,
    val shippingFee: Double,
    val paymentTypeId: Int?,
    val note: String?,
    val requiredNote: String?,
    val coupon: String?,
    val pickShift: List<Int>?
)

data class ShopOrderRequestDomainEntity(
    val shopId: String,
    val receiver: ReceiverDomainEntity,
    val cartItemIds: List<String>,
    val discountCodes: List<String>?,
    val shippingInfo: ShippingInfoDomainEntity?,
    val isCod: Boolean?
)

data class CreateOrderRequestDomainEntity(
    val shops: List<ShopOrderRequestDomainEntity>,
    val platformDiscountCodes: List<String>?
)

data class CreateOrderResponseDomainEntity(
    val orders: List<OrderDomainEntity>,
    val paymentId: Int // Sửa từ String
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