package com.ptit.data.mapping

import android.util.Log
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
    currentUses = usesCount ?: 0,
    brands = brands?.map { it.toDomainEntity() },
    categories = categories?.map { it.toDomainEntity() },
    products = products?.map { it.toDomainEntity() },
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: ""
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

fun DiscountDetailDto.toDomainEntity() = DiscountDomainEntity(
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
    currentUses = usesCount ?: 0,
    brands = null, // Detail response không có brands
    categories = null, // Detail response không có categories
    products = null, // Detail response không có products
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: ""
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

fun GetDiscountDetailResponseDto.toDomainEntity(): DiscountDomainEntity {
    return if (data == null) {
        Log.e("DiscountMapper", """
            ❌ GetDiscountDetailResponseDto.data is NULL
            - statusCode: $statusCode
            - message: $message
            - Full response: ${this}
        """.trimIndent())
        DiscountDomainEntity(
            id = "",
            name = "",
            description = null,
            value = 0.0,
            code = "",
            startDate = "",
            endDate = "",
            maxUsesPerUser = 0,
            minOrderValue = 0.0,
            maxUses = 0,
            maxDiscountValue = 0.0,
            displayType = "",
            voucherType = "",
            isPlatform = false,
            shopId = null,
            discountApplyType = "",
            discountStatus = "",
            discountType = "",
            currentUses = 0,
            brands = null,
            categories = null,
            products = null,
            createdAt = "",
            updatedAt = ""
        )
    }
    else data.toDomainEntity()
}

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

fun CreateDiscountResponseDto.toDomainEntity(): DiscountDomainEntity {
    return data?.toDomainEntity() ?: DiscountDomainEntity(
        id = "",
        name = "",
        description = null,
        value = 0.0,
        code = "",
        startDate = "",
        endDate = "",
        maxUsesPerUser = 0,
        minOrderValue = 0.0,
        maxUses = 0,
        maxDiscountValue = 0.0,
        displayType = "",
        voucherType = "",
        isPlatform = false,
        shopId = null,
        discountApplyType = "",
        discountStatus = "",
        discountType = "",
        currentUses = 0,
        brands = null,
        categories = null,
        products = null,
        createdAt = "",
        updatedAt = ""
    )
}

fun UpdateDiscountResponseDto.toDomainEntity(): DiscountDomainEntity {
    return data?.toDomainEntity() ?: DiscountDomainEntity(
        id = "",
        name = "",
        description = null,
        value = 0.0,
        code = "",
        startDate = "",
        endDate = "",
        maxUsesPerUser = 0,
        minOrderValue = 0.0,
        maxUses = 0,
        maxDiscountValue = 0.0,
        displayType = "",
        voucherType = "",
        isPlatform = false,
        shopId = null,
        discountApplyType = "",
        discountStatus = "",
        discountType = "",
        currentUses = 0,
        brands = null,
        categories = null,
        products = null,
        createdAt = "",
        updatedAt = ""
    )
}

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
    maxDiscountValue = maxDiscountValue ?: 10000000.0,
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

