package com.ptit.navigation.destination

import kotlinx.serialization.Serializable

@Serializable
data class EditDiscountRoute(
    val discountId: String,
    val shopId: String
)