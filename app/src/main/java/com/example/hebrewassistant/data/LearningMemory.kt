package com.example.hebrewassistant.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lesson_sessions",
    indices = [Index("status"), Index("createdAt")]
)
data class LessonSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String? = null,
    val status: String = STATUS_ACTIVE,
    val createdAt: Long,
    val completedAt: Long? = null
) {
    companion object {
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_COMPLETED = "COMPLETED"
    }
}

@Entity(
    tableName = "chat_messages",
    indices = [Index("sessionId"), Index("createdAt")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val text: String,
    val isUser: Boolean,
    val createdAt: Long
)

@Entity(
    tableName = "lesson_summaries",
    indices = [Index("sessionId"), Index("createdAt")]
)
data class LessonSummary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val topic: String,
    val summaryJson: String,
    val createdAt: Long
)
