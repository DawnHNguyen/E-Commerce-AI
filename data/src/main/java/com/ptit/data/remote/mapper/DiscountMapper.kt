package com.ptit.data.remote.mapper

import com.ptit.data.remote.dto.discount.*
import com.ptit.domain.entity.discount.*

// ==================== DTO to Domain Entity ====================

fun DiscountDto.toDomainEntity() = DiscountDomainEntity(
    id = id,
    name = name,
    description = description,
    value = value,
    code = code,
    startDate = startDate,
    endDate = endDate,
    maxUsesPerUser = maxUsesPerUser,
    minOrderValue = minOrderValue,
    maxUses = maxUses,
    maxDiscountValue = maxDiscountValue,
    displayType = displayType,
    voucherType = voucherType,
    isPlatform = isPlatform,
    shopId = shopId,
    discountApplyType = discountApplyType,
    discountStatus = discountStatus,
    discountType = discountType,
    currentUses = currentUses,
    brands = brands?.map { it.toDomainEntity() },
    categories = categories?.map { it.toDomainEntity() },
    products = products?.map { it.toDomainEntity() },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BrandItemDto.toDomainEntity() = BrandItemDomainEntity(
    id = id,
    name = name
)

fun CategoryItemDto.toDomainEntity() = CategoryItemDomainEntity(
    id = id,
    name = name
)

fun ProductItemDto.toDomainEntity() = ProductItemDomainEntity(
    id = id,
    name = name
)

fun GetAvailableDiscountsResponseDto.toDomainEntity() = DiscountListDomainEntity(
    data = data.map { it.toDomainEntity() }
)

fun GetManageDiscountsResponseDto.toDomainEntity() = DiscountListDomainEntity(
    data = data.map { it.toDomainEntity() },
    totalItems = metadata?.totalItems,
    page = metadata?.page,
    limit = metadata?.limit,
    totalPages = metadata?.totalPages,
    hasNext = metadata?.hasNext,
    hasPrev = metadata?.hasPrev
)

fun GetDiscountDetailResponseDto.toDomainEntity() = data.toDomainEntity()

fun ValidateVoucherCodeResponseDto.toDomainEntity() = ValidateVoucherResponseDomainEntity(
    isValid = isValid,
    discount = discount?.toDomainEntity(),
    error = error,
    discountAmount = discountAmount,
    finalOrderTotal = finalOrderTotal
)

fun ApplyVoucherResponseDto.toDomainEntity() = ApplyVoucherResponseDomainEntity(
    isValid = isValid,
    discount = discount?.toDomainEntity(),
    discountAmount = discountAmount,
    finalOrderTotal = finalOrderTotal,
    error = error
)

fun CreateDiscountResponseDto.toDomainEntity() = data.toDomainEntity()

fun UpdateDiscountResponseDto.toDomainEntity() = data.toDomainEntity()

// ==================== Domain Entity to DTO ====================

fun ValidateVoucherRequestDomainEntity.toDto() = ValidateVoucherCodeRequestDto(
    code = code,
    cartItemIds = cartItemIds
)

fun ApplyVoucherRequestDomainEntity.toDto() = ApplyVoucherRequestDto(
    code = code,
    orderId = orderId,
    cartItemIds = cartItemIds
)

fun CreateDiscountRequestDomainEntity.toDto() = CreateDiscountRequestDto(
    name = name,
    description = description,
    value = value,
    code = code,
    startDate = startDate,
    endDate = endDate,
    maxUsesPerUser = maxUsesPerUser,
    minOrderValue = minOrderValue,
    maxUses = maxUses,
    maxDiscountValue = maxDiscountValue,
    displayType = displayType,
    voucherType = voucherType,
    isPlatform = isPlatform,
    shopId = shopId,
    discountApplyType = discountApplyType,
    discountStatus = discountStatus,
    discountType = discountType,
    brands = brands,
    categories = categories,
    products = products
)

fun UpdateDiscountRequestDomainEntity.toDto() = UpdateDiscountRequestDto(
    name = name,
    description = description,
    value = value,
    code = code,
    startDate = startDate,
    endDate = endDate,
    maxUsesPerUser = maxUsesPerUser,
    minOrderValue = minOrderValue,
    maxUses = maxUses,
    maxDiscountValue = maxDiscountValue,
    displayType = displayType,
    voucherType = voucherType,
    isPlatform = isPlatform,
    shopId = shopId,
    discountApplyType = discountApplyType,
    discountStatus = discountStatus,
    discountType = discountType,
    brands = brands,
    categories = categories,
    products = products
)

