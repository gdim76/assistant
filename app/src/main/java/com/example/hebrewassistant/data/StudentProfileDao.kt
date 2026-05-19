package com.example.hebrewassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StudentProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: StudentProfile)

    @Query("SELECT * FROM student_profile WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: Int = StudentProfile.SINGLE_PROFILE_ID): StudentProfile?
}
