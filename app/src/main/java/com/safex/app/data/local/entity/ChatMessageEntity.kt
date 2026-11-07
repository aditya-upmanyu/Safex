package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.safex.app.domain.chatbot.model.*

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String,
    val contentType: String,
    val text: String,
    val payloadJson: String?,
    val scriptId: String?,
    val language: String,
    val status: String,
    val feedback: String,
    val createdAt: Long
) {
    fun toDomain(): ChatMessage {
        val mappedRole = try {
            MessageRole.valueOf(role)
        } catch (_: Exception) {
            MessageRole.ASSISTANT
        }
        val mappedContentType = try {
            MessageContentType.valueOf(contentType)
        } catch (_: Exception) {
            MessageContentType.TEXT
        }
        val mappedStatus = try {
            MessageStatus.valueOf(status)
        } catch (_: Exception) {
            MessageStatus.SENT
        }
        val mappedFeedback = try {
            FeedbackStatus.valueOf(feedback)
        } catch (_: Exception) {
            FeedbackStatus.NONE
        }

        return ChatMessage(
            id = id,
            sessionId = sessionId,
            role = mappedRole,
            contentType = mappedContentType,
            text = text,
            payloadJson = payloadJson,
            scriptId = scriptId,
            language = ChatLanguage.fromCode(language),
            status = mappedStatus,
            feedback = mappedFeedback,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(domain: ChatMessage): ChatMessageEntity = ChatMessageEntity(
            id = domain.id,
            sessionId = domain.sessionId,
            role = domain.role.name,
            contentType = domain.contentType.name,
            text = domain.text,
            payloadJson = domain.payloadJson,
            scriptId = domain.scriptId,
            language = domain.language.code,
            status = domain.status.name,
            feedback = domain.feedback.name,
            createdAt = domain.createdAt
        )
    }
}
