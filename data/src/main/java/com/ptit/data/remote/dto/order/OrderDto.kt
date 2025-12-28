package com.ptit.data.remote.dto.order

import com.google.gson.annotations.SerializedName

// 🧱 Product SKU Snapshot trong đơn hàng
data class ProductSKUSnapshotDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String?,
    @SerializedName("productName") val productName: String,
    @SerializedName("skuPrice") val skuPrice: Int,
    @SerializedName("originalPrice") val originalPrice: Int?, // Giá gốc (trước giảm)
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
    @SerializedName("address") val address: String,

    // 🔴 MỚI: Thêm các trường địa chỉ GHN
    @SerializedName("provinceId") val provinceId: Int?,
    @SerializedName("districtId") val districtId: Int?,
    @SerializedName("wardCode") val wardCode: String?
)
data class ShippingInfoDto(
    @SerializedName("service_id") val serviceId: Int?,
    @SerializedName("service_type_id") val serviceTypeId: Int?,
    @SerializedName("config_fee_id") val configFeeId: String?,
    @SerializedName("extra_cost_id") val extraCostId: String?,
    @SerializedName("weight") val weight: Double,
    @SerializedName("length") val length: Double,
    @SerializedName("width") val width: Double,
    @SerializedName("height") val height: Double,
    @SerializedName("shippingFee") val shippingFee: Double,
    @SerializedName("payment_type_id") val paymentTypeId: Int?,
    @SerializedName("note") val note: String?,
    @SerializedName("required_note") val requiredNote: String?,
    @SerializedName("coupon") val coupon: String?,
    @SerializedName("pick_shift") val pickShift: List<Int>?
)
data class ShopOrderRequestDto(
    @SerializedName("shopId") val shopId: String,
    @SerializedName("receiver") val receiver: ReceiverDto,
    @SerializedName("cartItemIds") val cartItemIds: List<String>,
    @SerializedName("discountCodes") val discountCodes: List<String>?,
    @SerializedName("shippingInfo") val shippingInfo: ShippingInfoDto?,
    @SerializedName("isCod") val isCod: Boolean?
)

// 🆕 Request tạo đơn hàng
data class CreateOrderRequestDto(
    @SerializedName("shops") val shops: List<ShopOrderRequestDto>,
    @SerializedName("platformDiscountCodes") val platformDiscountCodes: List<String>?
)

// 🧾 Đơn hàng
data class OrderDto(
    @SerializedName("id") val id: String?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("shopId") val shopId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("totalAmount") val totalAmount: Int?,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("receiver") val receiver: ReceiverDto?,
    @SerializedName("items") val items: List<ProductSKUSnapshotDto>?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("orderCode") val orderCode: String?,

    @SerializedName("totalItemCost") val totalItemCost: Int?,
    @SerializedName("totalShippingFee") val totalShippingFee: Int?,
    @SerializedName("totalVoucherDiscount") val totalVoucherDiscount: Int?,
    @SerializedName("totalPayment") val totalPayment: Int?
)

// 📋 Danh sách đơn hàng (GET /orders)
data class GetOrderListResponseDto(
    @SerializedName("data") val data: List<OrderDto>,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)


data class CreateOrderDataDto(
    @SerializedName("orders") val orders: List<OrderDto>?,
    // 🔴 SỬA: paymentId là Int (number), không phải String
    @SerializedName("paymentId") val paymentId: Int?
)

// ❌ Response khi hủy đơn hàng
data class CancelOrderResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String
)



