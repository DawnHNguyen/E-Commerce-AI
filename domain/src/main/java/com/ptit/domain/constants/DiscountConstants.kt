package com.ptit.domain.constants

object DiscountConstants {
    // Discount Status
    const val DISCOUNT_STATUS_ACTIVE = "ACTIVE"
    const val DISCOUNT_STATUS_INACTIVE = "INACTIVE"
    const val DISCOUNT_STATUS_EXPIRED = "EXPIRED"
    const val DISCOUNT_STATUS_SCHEDULED = "SCHEDULED"

    // Discount Type
    const val DISCOUNT_TYPE_PERCENTAGE = "PERCENTAGE"
    const val DISCOUNT_TYPE_FIXED = "FIXED"

    // Discount Apply Type
    const val DISCOUNT_APPLY_TYPE_ALL = "ALL"
    const val DISCOUNT_APPLY_TYPE_SPECIFIC = "SPECIFIC"

    // Voucher Type (Updated to match API)
    const val VOUCHER_TYPE_SHOP = "SHOP"
    const val VOUCHER_TYPE_PRODUCT = "PRODUCT"
    const val VOUCHER_TYPE_PLATFORM = "PLATFORM"
    const val VOUCHER_TYPE_CATEGORY = "CATEGORY"
    const val VOUCHER_TYPE_BRAND = "BRAND"

    // Display Type (Updated to match API)
    const val DISPLAY_TYPE_PUBLIC = "PUBLIC"
    const val DISPLAY_TYPE_PRIVATE = "PRIVATE"
}

