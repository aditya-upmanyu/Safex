package com.safex.app.domain.chatbot.model

enum class EmergencyLevel {
    NONE,
    URGENT,
    EMERGENCY
}

data class EmergencyDetectionResult(
    val level: EmergencyLevel,
    val topicId: String?,
    val isEmergency: Boolean,
    val matchedKeyword: String? = null
)

data class IntentMatchResult(
    val bestTopicId: String?,
    val score: Int,
    val alternatives: List<String> = emptyList()
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM_CARD
}

enum class MessageContentType {
    TEXT,
    SCRIPT_CARD,
    AI_CARD,
    CHECKER_RESULT,
    TRIAGE,
    EMERGENCY
}

enum class MessageStatus {
    SENT,
    FAILED,
    PENDING
}

enum class FeedbackStatus {
    NONE,
    UP,
    DOWN
}

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val contentType: MessageContentType,
    val text: String,
    val payloadJson: String? = null,
    val scriptId: String? = null,
    val language: ChatLanguage,
    val status: MessageStatus = MessageStatus.SENT,
    val feedback: FeedbackStatus = FeedbackStatus.NONE,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTopicId: String? = null,
    val language: ChatLanguage = ChatLanguage.EN
)

data class AiReply(
    val verdict: Verdict,
    val headline: String,
    val why: List<String>,
    val doNow: List<String>,
    val dont: List<String>,
    val reportTo: List<String>,
    val suggestedTopicIds: List<String>,
    val followUpQuestions: List<String>
)
