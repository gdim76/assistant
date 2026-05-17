package com.example.hebrewassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: LessonProgress)

    @Query("SELECT * FROM lesson_progress ORDER BY createdAt DESC")
    suspend fun getAll(): List<LessonProgress>
}
