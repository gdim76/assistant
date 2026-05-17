package com.example.hebrewassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topic: String,
    val summary: String,
    val createdAt: Long
)
