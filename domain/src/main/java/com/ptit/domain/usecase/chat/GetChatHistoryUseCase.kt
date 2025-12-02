package com.ptit.domain.usecase.chat

import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatHistoryUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(sessionId: String): Flow<List<ChatMessage>> {
        return chatRepository.getChatHistory(sessionId)
    }
}