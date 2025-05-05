package com.ptit.navigation.destination
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRoute(val selectedItemIds: List<String>)
