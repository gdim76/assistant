package com.example.hebrewassistant.llm

import com.example.hebrewassistant.data.LessonProgress
import com.example.hebrewassistant.data.ProgressDao
import com.example.hebrewassistant.data.SettingsRepository
import java.time.Instant
import kotlinx.coroutines.flow.first

class LlmRepository(
    private val serviceFactory: LlmServiceFactory,
    private val settingsRepository: SettingsRepository,
    private val progressDao: ProgressDao
) {
    suspend fun generateLesson(topic: String): LlmResponse {
        val settings = settingsRepository.settingsFlow.first()
        val service = serviceFactory.create(settings.provider, settings.apiKey)
        val prompt = LlmPromptTemplates.buildLessonPrompt(topic)
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
        val prompt = LlmPromptTemplates.buildChatPrompt(message)
        return service.requestCompletion(LlmRequest(prompt))
    }
}
