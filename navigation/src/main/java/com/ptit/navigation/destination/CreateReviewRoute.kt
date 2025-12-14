package com.ptit.navigation.destination

import kotlinx.serialization.Serializable

@Serializable
data class CreateReviewRoute(
    val orderId: String,
    val productId: String,
    val productName: String,
    val productImage: String,
    val productPrice: Int,
    val productSkuValue: String
)

