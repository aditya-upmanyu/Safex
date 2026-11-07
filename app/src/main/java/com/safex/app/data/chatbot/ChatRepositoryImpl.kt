package com.safex.app.data.chatbot

import com.safex.app.data.local.dao.ChatDao
import com.safex.app.data.local.entity.ChatMessageEntity
import com.safex.app.data.local.entity.ChatSessionEntity
import com.safex.app.domain.chatbot.model.ChatMessage
import com.safex.app.domain.chatbot.model.ChatSession
import com.safex.app.domain.chatbot.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getMessages(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessages(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMessage(message: ChatMessage) {
        chatDao.insertMessage(ChatMessageEntity.fromDomain(message))
    }

    override suspend fun updateMessage(message: ChatMessage) {
        chatDao.updateMessage(ChatMessageEntity.fromDomain(message))
    }

    override suspend fun getSession(sessionId: String): ChatSession? {
        return chatDao.getSession(sessionId)?.toDomain()
    }

    suspend fun getLatestSession(): ChatSession? {
        return chatDao.getLatestSession()?.toDomain()
    }

    override suspend fun createSession(session: ChatSession) {
        chatDao.insertSession(ChatSessionEntity.fromDomain(session))
    }

    override suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }

    override suspend fun deleteAllChats() {
        chatDao.deleteAllChats()
    }
}
