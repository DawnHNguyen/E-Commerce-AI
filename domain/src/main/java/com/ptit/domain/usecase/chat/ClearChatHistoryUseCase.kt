package com.ptit.domain.usecase.chat

import com.ptit.domain.repository.ChatRepository
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class ClearChatHistoryUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: String): Resource<Unit> {
        return chatRepository.clearChatHistory(sessionId)
    }
}