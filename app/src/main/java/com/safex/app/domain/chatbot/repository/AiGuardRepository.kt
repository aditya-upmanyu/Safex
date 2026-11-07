package com.safex.app.domain.chatbot.repository

import com.safex.app.domain.chatbot.model.AiReply
import com.safex.app.domain.chatbot.model.ChatLanguage

sealed interface AiResult {
    data class Success(val reply: AiReply) : AiResult
    data object Offline : AiResult
    data object Timeout : AiResult
    data class QuotaExceeded(val retryAfterMinutes: Int) : AiResult
    data class Blocked(val reason: String) : AiResult
    data class ServerError(val message: String) : AiResult
    data class ParseFallback(val rawText: String) : AiResult
    data object Disabled : AiResult
}

data class AiRequest(
    val language: ChatLanguage,
    val currentTopicId: String?,
    val topicSummary: String?,
    val history: List<Pair<String, String>>, // role to text, max 6
    val userText: String
)

interface AiGuardRepository {
    suspend fun ask(request: AiRequest): AiResult
}
