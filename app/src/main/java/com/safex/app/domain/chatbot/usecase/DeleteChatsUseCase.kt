package com.safex.app.domain.chatbot.usecase

import com.safex.app.domain.chatbot.repository.ChatRepository
import javax.inject.Inject

class DeleteChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend fun deleteAll() {
        chatRepository.deleteAllChats()
    }

    suspend fun deleteSession(sessionId: String) {
        chatRepository.deleteSession(sessionId)
    }
}
