package com.ptit.data.mapping

import com.ptit.data.remote.dto.order.CancelOrderResponseDto
import com.ptit.data.remote.dto.order.CreateOrderDataDto
import com.ptit.data.remote.dto.order.CreateOrderRequestDto
import com.ptit.data.remote.dto.order.GetOrderListResponseDto
import com.ptit.data.remote.dto.order.OrderDto
import com.ptit.data.remote.dto.order.ProductSKUSnapshotDto
import com.ptit.data.remote.dto.order.ReceiverDto
import com.ptit.data.remote.dto.order.ShippingInfoDto
import com.ptit.data.remote.dto.order.ShopOrderRequestDto
import com.ptit.domain.entity.order.CancelOrderResponseDomainEntity
import com.ptit.domain.entity.order.CreateOrderRequestDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.GetOrderListDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.entity.order.ProductSKUSnapshotDomainEntity
import com.ptit.domain.entity.order.ReceiverDomainEntity
import com.ptit.domain.entity.order.ShippingInfoDomainEntity
import com.ptit.domain.entity.order.ShopOrderRequestDomainEntity

fun ProductSKUSnapshotDto.toDomainEntity() = ProductSKUSnapshotDomainEntity(
    id = id,
    productId = productId,
    productName = productName,
    skuPrice = skuPrice,
    originalPrice = originalPrice,
    image = image,
    skuValue = skuValue,
    skuId = skuId,
    orderId = orderId,
    quantity = quantity,
    createdAt = createdAt
)

// 🔴 SỬA: Thêm các trường GHN
fun ReceiverDto.toDomainEntity() = ReceiverDomainEntity(
    name = name,
    phone = phone,
    address = address,
    provinceId = provinceId,
    districtId = districtId,
    wardCode = wardCode
)

fun OrderDto.toDomainEntity() = OrderDomainEntity(
    id = id ?: "",
    userId = userId ?: "",
    shopId = shopId ?: "",
    status = status ?: "",
    totalAmount = totalAmount ?: 0,
    paymentMethod = paymentMethod ?: "",
    receiver = receiver?.toDomainEntity(),
    items = items?.map { it.toDomainEntity() } ?: emptyList(),
    createdAt = createdAt ?: "",
    updatedAt = updatedAt,
    orderCode = orderCode, // Mới
    totalItemCost = totalItemCost ?: totalAmount ?: 0,
    totalShippingFee = totalShippingFee ?: 0,
    totalVoucherDiscount = totalVoucherDiscount ?: 0,
    totalPayment = totalPayment ?: totalAmount ?: 0
)

// ---------------------------------------------------
// 🔴 MỚI: MAPPERS TỪ DOMAIN -> DTO (Cho Request)
// ---------------------------------------------------

fun ReceiverDomainEntity.toDto() = ReceiverDto(
    name = name,
    phone = phone,
    address = address,
    provinceId = provinceId,
    districtId = districtId,
    wardCode = wardCode
)

fun ShippingInfoDomainEntity.toDto() = ShippingInfoDto(
    serviceId = serviceId,
    serviceTypeId = serviceTypeId,
    configFeeId = configFeeId,
    extraCostId = extraCostId,
    weight = weight,
    length = length,
    width = width,
    height = height,
    shippingFee = shippingFee,
    paymentTypeId = paymentTypeId,
    note = note,
    requiredNote = requiredNote,
    coupon = coupon,
    pickShift = pickShift
)

fun ShopOrderRequestDomainEntity.toDto() = ShopOrderRequestDto(
    shopId = shopId,
    receiver = receiver.toDto(),
    cartItemIds = cartItemIds,
    discountCodes = discountCodes,
    shippingInfo = shippingInfo?.toDto(),
    isCod = isCod
)

fun CreateOrderRequestDomainEntity.toDto() = CreateOrderRequestDto(
    shops = shops.map { it.toDto() },
    platformDiscountCodes = platformDiscountCodes
)

// 🔴 SỬA: Mapper cho response mới
fun CreateOrderDataDto.toDomainEntity() = CreateOrderResponseDomainEntity(
    // Xử lý data lồng bên trong
    orders = this.orders?.map { it.toDomainEntity() } ?: emptyList(),
    paymentId = this.paymentId ?: 0 // Sửa kiểu
)

fun CancelOrderResponseDto.toDomainEntity() = CancelOrderResponseDomainEntity(
    id = id,
    status = status
)

fun GetOrderListResponseDto.toDomainEntity() = GetOrderListDomainEntity(
    data = data.map { it.toDomainEntity() },
    totalItems = totalItems,
    page = page,
    limit = limit,
    totalPages = totalPages
)