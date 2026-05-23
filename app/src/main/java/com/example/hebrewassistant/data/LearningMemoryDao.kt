package com.example.hebrewassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LearningMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LessonSession): Long

    @Query("SELECT * FROM lesson_sessions WHERE status = :status ORDER BY createdAt DESC, id DESC LIMIT 1")
    suspend fun getLatestSessionByStatus(status: String = LessonSession.STATUS_ACTIVE): LessonSession?

    @Query("UPDATE lesson_sessions SET status = :status, completedAt = :completedAt WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: Long, status: String, completedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query(
        """
        SELECT * FROM chat_messages
        WHERE sessionId = :sessionId
        ORDER BY createdAt DESC, id DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentMessages(sessionId: Long, limit: Int): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    suspend fun getSessionMessages(sessionId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonSummary(summary: LessonSummary): Long

    @Query(
        """
        SELECT * FROM lesson_summaries
        ORDER BY createdAt DESC, id DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentLessonSummaries(limit: Int): List<LessonSummary>
}
