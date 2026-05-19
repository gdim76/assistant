package com.example.hebrewassistant.llm

import com.example.hebrewassistant.data.LessonProgress
import com.example.hebrewassistant.data.ProgressDao
import com.example.hebrewassistant.data.SettingsRepository
import com.example.hebrewassistant.data.StudentProfileRepository
import java.time.Instant
import kotlinx.coroutines.flow.first

class LlmRepository(
    private val serviceFactory: LlmServiceFactory,
    private val settingsRepository: SettingsRepository,
    private val progressDao: ProgressDao,
    private val studentProfileRepository: StudentProfileRepository
) {
    suspend fun generateLesson(topic: String): LlmResponse {
        val settings = settingsRepository.settingsFlow.first()
        val service = serviceFactory.create(settings.provider, settings.apiKey)
        val prompt = LlmPromptTemplates.buildLessonPrompt(topic, studentProfileRepository.getProfile())
        val response = service.requestCompletion(LlmRequest(prompt))
        progressDao.insert(
            LessonProgress(
                topic = topic,
                summary = response.output.take(120),
                createdAt = Instant.now().toEpochMilli()
            )
        )
        return response
    }

    suspend fun loadProgress(): List<LessonProgress> {
        return progressDao.getAll()
    }

    suspend fun chat(message: String): LlmResponse {
        val settings = settingsRepository.settingsFlow.first()
        val service = serviceFactory.create(settings.provider, settings.apiKey)
        val profile = studentProfileRepository.getProfile()
        val prompt = if (profile?.onboardingCompleted == true) {
            LlmPromptTemplates.buildChatPrompt(message, profile)
        } else {
            LlmPromptTemplates.buildOnboardingPrompt(message, profile)
        }
        val response = service.requestCompletion(LlmRequest(prompt))
        val profileJson = LlmPromptTemplates.extractStudentProfileJson(response.output)
        return if (profileJson != null) {
            studentProfileRepository.saveOnboardingProfile(profileJson)
            LlmResponse(LlmPromptTemplates.stripStudentProfileBlock(response.output))
        } else {
            response
        }
    }

    suspend fun initialAssistantMessage(): String {
        return if (studentProfileRepository.isOnboardingCompleted()) {
            "שלום! Чем займемся сегодня: разговорная практика, грамматика, словарь или тема из работы?"
        } else {
            LlmPromptTemplates.buildInitialOnboardingMessage()
        }
    }
}
