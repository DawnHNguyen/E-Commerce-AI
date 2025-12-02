package com.ptit.domain.repository

import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.ChatSession
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // Session management
    fun getAllSessions(): Flow<List<ChatSession>>
    suspend fun createSession(title: String = "Cuộc trò chuyện mới"): Resource<ChatSession>
    suspend fun deleteSession(sessionId: String): Resource<Unit>

    // Message operations
    suspend fun sendMessage(sessionId: String, content: String): Flow<Resource<Unit>>
    fun getChatHistory(sessionId: String): Flow<List<ChatMessage>>
    suspend fun clearChatHistory(sessionId: String): Resource<Unit>
}