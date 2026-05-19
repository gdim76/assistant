package com.example.hebrewassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey
    val id: Int = SINGLE_PROFILE_ID,
    val name: String? = null,
    val nativeLanguage: String? = null,
    val interests: String? = null,
    val workArea: String? = null,
    val learningGoal: String? = null,
    val currentLevel: String? = null,
    val placementSummary: String? = null,
    val onboardingCompleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        const val SINGLE_PROFILE_ID = 1
    }
}
