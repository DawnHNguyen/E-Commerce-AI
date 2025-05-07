package com.ptit.navigation.destination

import kotlinx.serialization.Serializable

@Serializable
data object ListPaymentMethodRoute

@Serializable
data object ConfigPaymentMethodRoute

@Serializable
data object UpdateShopRoute

@Serializable
data object ShopDetailRoute

@Serializable
data object ProductListRoute

@Serializable
data class ProductFormRoute (val productId: String? = null)
