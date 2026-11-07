package com.safex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.model.ChatSession

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val lastTopicId: String?,
    val language: String
) {
    fun toDomain(): ChatSession = ChatSession(
        id = id,
        title = title,
        createdAt = createdAt,
        lastTopicId = lastTopicId,
        language = ChatLanguage.fromCode(language)
    )

    companion object {
        fun fromDomain(domain: ChatSession): ChatSessionEntity = ChatSessionEntity(
            id = domain.id,
            title = domain.title,
            createdAt = domain.createdAt,
            lastTopicId = domain.lastTopicId,
            language = domain.language.code
        )
    }
}
