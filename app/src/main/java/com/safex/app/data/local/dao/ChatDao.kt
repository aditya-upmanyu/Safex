package com.safex.app.data.local.dao

import androidx.room.*
import com.safex.app.data.local.entity.ChatMessageEntity
import com.safex.app.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: String): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestSession(): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionOnly(sessionId: String)

    @Transaction
    suspend fun deleteSession(sessionId: String) {
        deleteMessagesForSession(sessionId)
        deleteSessionOnly(sessionId)
    }

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()

    @Transaction
    suspend fun deleteAllChats() {
        deleteAllMessages()
        deleteAllSessions()
    }

    @Query("DELETE FROM chat_messages WHERE createdAt < :cutoffTimestamp")
    suspend fun deleteMessagesOlderThan(cutoffTimestamp: Long)
}
