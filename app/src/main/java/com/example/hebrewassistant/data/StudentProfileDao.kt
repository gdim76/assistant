package com.example.hebrewassistant.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface StudentProfileDao {
    @Upsert
    suspend fun upsert(profile: StudentProfile)

    @Query("SELECT * FROM student_profile WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: Int = StudentProfile.SINGLE_PROFILE_ID): StudentProfile?
}
