package com.example.hebrewassistant.data

import java.time.Instant
import org.json.JSONObject

class StudentProfileRepository(
    private val studentProfileDao: StudentProfileDao
) {
    suspend fun getProfile(): StudentProfile? {
        return studentProfileDao.getProfile()
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return getProfile()?.onboardingCompleted == true
    }

    suspend fun saveOnboardingProfile(profileJson: String): StudentProfile {
        val json = JSONObject(profileJson)
        val now = Instant.now().toEpochMilli()
        val existing = getProfile()
        val createdAt = existing?.createdAt ?: now
        val profile = StudentProfile(
            name = json.optNullableString("name") ?: existing?.name,
            nativeLanguage = json.optNullableString("nativeLanguage") ?: existing?.nativeLanguage,
            interests = json.optNullableString("interests") ?: existing?.interests,
            workArea = json.optNullableString("workArea") ?: existing?.workArea,
            learningGoal = json.optNullableString("learningGoal") ?: existing?.learningGoal,
            currentLevel = json.optNullableString("currentLevel") ?: existing?.currentLevel,
            placementSummary = json.optNullableString("placementSummary") ?: existing?.placementSummary,
            onboardingCompleted = true,
            createdAt = createdAt,
            updatedAt = now
        )
        studentProfileDao.upsert(profile)
        return profile
    }

    private fun JSONObject.optNullableString(name: String): String? {
        return if (has(name) && !isNull(name)) {
            optString(name).takeIf { it.isNotBlank() }
        } else {
            null
        }
    }
}
