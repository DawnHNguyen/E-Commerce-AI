package com.ptit.navigation.destination

import kotlinx.serialization.Serializable

@Serializable
data class ProductsByCategoryRoute(
    val categoryId: String,
    val categoryDisplayName: String
)