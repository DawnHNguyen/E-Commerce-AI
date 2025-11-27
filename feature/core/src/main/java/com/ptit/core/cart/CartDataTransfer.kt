package com.ptit.core.cart

import com.ptit.domain.entity.cart.CartItemDetailDomainEntity

/**
 * Object Singleton dùng để truyền dữ liệu giỏ hàng (groupedItems)
 * giữa CartScreen và CreateOrderScreen, tránh truyền qua NavArguments.
 */
object CartDataTransfer {
    // Sử dụng biến lateinit hoặc MutableList để lưu trữ dữ liệu
    var groupedItems: List<CartItemDetailDomainEntity>? = null

    fun clear() {
        groupedItems = null
    }
}