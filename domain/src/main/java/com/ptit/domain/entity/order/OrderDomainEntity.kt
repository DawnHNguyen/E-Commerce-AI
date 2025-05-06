package com.ptit.domain.entity.order

import com.ptit.domain.entity.cart.PurchaseDomainEntity

data class OrderDomainEntity(
    val id: String = "",
    val purchases: List<PurchaseDomainEntity> = emptyList(),
    val shippingFee: Int = 0,
    val status: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val totalAmount: Int = 0,
    val paymentMethod: String = "",
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val paidAt: String? = null,
    val paymentGatewayResponse: String? = null,
    val recurlyAccountId: String? = null,
    val recurlyTransactionId: String? = null
) {
    val subTotal by lazy {
        purchases.sumOf { it.product.price * it.buyCount }
    }

    val totalPrice by lazy {
        subTotal + shippingFee
    }
}