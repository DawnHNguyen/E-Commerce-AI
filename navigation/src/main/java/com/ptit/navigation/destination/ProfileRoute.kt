package com.ptit.navigation.destination

import kotlinx.serialization.Serializable

@Serializable
data object ListPaymentMethodRoute

@Serializable
data object ConfigPaymentMethodRoute

@Serializable
data object UpdateShopRoute

@Serializable
data object CreateSellerRequestRoute

@Serializable
data object ShopEntryRoute

@Serializable
data class ShopDetailRoute(val shopId: String)

@Serializable
data object RequestStatusRoute

@Serializable
data object ProductListRoute

@Serializable
data class ProductFormRoute (val productId: String? = null)

@Serializable
data object EditProfileRoute

@Serializable
data object AddAddressRoute

@Serializable
data object ChangePasswordRoute

@Serializable
data object ProfileRoute

@Serializable
data object OrderHistoryRoute
