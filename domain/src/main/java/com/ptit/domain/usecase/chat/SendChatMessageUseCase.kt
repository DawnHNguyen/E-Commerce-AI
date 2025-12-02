package com.ptit.domain.usecase.chat

import com.ptit.domain.repository.ChatRepository
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: String, message: String): Flow<Resource<Unit>> {
        return chatRepository.sendMessage(sessionId, message)
    }
}