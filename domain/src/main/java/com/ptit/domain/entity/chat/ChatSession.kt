package com.ptit.domain.entity.chat

data class ChatSession(
    val id: String,
    val title: String,
    val lastMessage: String,
    val createdAt: Long,
    val updatedAt: Long
)