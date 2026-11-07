package com.safex.app.domain.chatbot.repository

import com.safex.app.domain.chatbot.model.ChatMessage
import com.safex.app.domain.chatbot.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(sessionId: String): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage)
    suspend fun updateMessage(message: ChatMessage)
    suspend fun getSession(sessionId: String): ChatSession?
    suspend fun createSession(session: ChatSession)
    suspend fun deleteSession(sessionId: String)
    suspend fun deleteAllChats()
}
