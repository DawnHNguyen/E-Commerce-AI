package com.ptit.navigation.destination

import kotlinx.serialization.Serializable

@Serializable
data object ChatSessionListRoute

@Serializable
data class ChatRoute(val sessionId: String? = null)