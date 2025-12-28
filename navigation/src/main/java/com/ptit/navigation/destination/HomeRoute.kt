package com.ptit.navigation.destination

import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailRoute(val productId: String)

@Serializable
data object SearchRoute

@Serializable
data object RecommendationsRoute
