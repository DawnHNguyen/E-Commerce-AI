package com.ptit.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import comptitdatabase.GetAllChatSessions
import com.ptit.database.PtitEcomDatabase
import comptitdatabase.TblChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatLocalDataSource @Inject constructor(
    private val database: PtitEcomDatabase
) {
    // Session operations
    suspend fun insertChatSession(
        sessionId: String = UUID.randomUUID().toString(),
        title: String = "Cuộc trò chuyện mới"
    ): String = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        database.ecomDatabaseQueries.insertChatSession(
            id = sessionId,
            title = title,
            createdAt = timestamp,
            updatedAt = timestamp
        )
        sessionId
    }

    fun getAllSessionsFlow(): Flow<List<GetAllChatSessions>> {
        return database.ecomDatabaseQueries
            .getAllChatSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        database.ecomDatabaseQueries.deleteChatSession(sessionId)
    }

    suspend fun updateSessionTimestamp(sessionId: String) = withContext(Dispatchers.IO) {
        database.ecomDatabaseQueries.updateSessionTimestamp(
            sessionId = sessionId,
            updatedAt = System.currentTimeMillis()
        )
    }

    // Message operations
    suspend fun insertChatMessage(
        id: String = UUID.randomUUID().toString(),
        sessionId: String,
        role: String,
        content: String,
        tags: String = "",
        isPending: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        database.ecomDatabaseQueries.insertChatMessage(
            id = id,
            sessionId = sessionId,
            role = role,
            content = content,
            timestamp = System.currentTimeMillis(),
            tags = tags,
            isPending = if (isPending) 1L else 0L
        )
        // Update session timestamp when new message is added
        updateSessionTimestamp(sessionId)
        id
    }

    fun getAllChatMessagesFlow(sessionId: String): Flow<List<TblChatMessage>> {
        return database.ecomDatabaseQueries
            .getAllChatMessagesFlow(sessionId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun getAllChatMessages(sessionId: String): List<TblChatMessage> = withContext(Dispatchers.IO) {
        database.ecomDatabaseQueries.getAllChatMessages(sessionId).executeAsList()
    }

    suspend fun updateMessagePendingStatus(messageId: String, isPending: Boolean) = withContext(Dispatchers.IO) {
        database.ecomDatabaseQueries.updateMessagePendingStatus(
            messageId = messageId,
            isPending = if (isPending) 1L else 0L
        )
    }

    suspend fun deleteChatHistory(sessionId: String) = withContext(Dispatchers.IO) {
        database.ecomDatabaseQueries.deleteChatHistory(sessionId)
    }
}