package com.example.hebrewassistant.llm

import com.example.hebrewassistant.data.ChatMessage
import com.example.hebrewassistant.data.ChatMessageEntity
import com.example.hebrewassistant.data.LearningMemoryDao
import com.example.hebrewassistant.data.LessonProgress
import com.example.hebrewassistant.data.LessonSession
import com.example.hebrewassistant.data.LessonSummary
import com.example.hebrewassistant.data.ProgressDao
import com.example.hebrewassistant.data.SettingsRepository
import com.example.hebrewassistant.data.StudentProfileRepository
import java.time.Instant
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class LlmRepository(
    private val serviceFactory: LlmServiceFactory,
    private val settingsRepository: SettingsRepository,
    private val progressDao: ProgressDao,
    private val studentProfileRepository: StudentProfileRepository,
    private val learningMemoryDao: LearningMemoryDao
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

    suspend fun chat(message: String, conversation: List<ChatMessage> = emptyList()): LlmResponse {
        val settings = settingsRepository.settingsFlow.first()
        val service = serviceFactory.create(settings.provider, settings.apiKey)
        val profile = studentProfileRepository.getProfile()
        val sessionId = getOrCreateActiveSessionId()
        saveChatMessage(sessionId, message, isUser = true)
        val recentConversation = loadRecentConversation(sessionId).ifEmpty { conversation.takeLast(CHAT_WINDOW_SIZE) }
        val recentSummaries = learningMemoryDao.getRecentLessonSummaries(LESSON_SUMMARY_WINDOW_SIZE)
        val progress = progressDao.getAll()
        val prompt = if (profile?.onboardingCompleted == true) {
            val shouldStartStarterLesson = progress.size < 3 && LlmPromptTemplates.shouldStartStarterLesson(message)
            if (shouldStartStarterLesson) {
                val lessonNumber = LlmPromptTemplates.nextStarterLessonNumber(progress.size)
                LlmPromptTemplates.buildStarterLessonPrompt(lessonNumber, profile, recentConversation, recentSummaries)
            } else {
                LlmPromptTemplates.buildChatPrompt(message, profile, recentConversation, recentSummaries)
            }
        } else {
            LlmPromptTemplates.buildOnboardingPrompt(message, profile, recentConversation)
        }
        val response = service.requestCompletion(LlmRequest(prompt))
        val profileJson = LlmPromptTemplates.extractStudentProfileJson(response.output)
        if (profile?.onboardingCompleted == true) {
            saveStarterLessonIfNeeded(message, response, progress.size)
        }
        val visibleResponse = if (profileJson != null) {
            studentProfileRepository.saveOnboardingProfile(profileJson)
            LlmResponse(LlmPromptTemplates.stripStudentProfileBlock(response.output))
        } else {
            response
        }
        saveChatMessage(sessionId, visibleResponse.output, isUser = false)
        return visibleResponse
    }

    suspend fun finishCurrentLesson(): LlmResponse {
        val session = learningMemoryDao.getLatestSessionByStatus()
            ?: return LlmResponse("Нет активного урока для завершения.")
        val messages = learningMemoryDao.getSessionMessages(session.id).map { it.toChatMessage() }
        if (messages.isEmpty()) {
            learningMemoryDao.updateSessionStatus(
                sessionId = session.id,
                status = LessonSession.STATUS_COMPLETED,
                completedAt = Instant.now().toEpochMilli()
            )
            return LlmResponse("Урок завершен, но сообщений для сводки пока нет.")
        }

        val settings = settingsRepository.settingsFlow.first()
        val service = serviceFactory.create(settings.provider, settings.apiKey)
        val profile = studentProfileRepository.getProfile()
        val recentSummaries = learningMemoryDao.getRecentLessonSummaries(LESSON_SUMMARY_WINDOW_SIZE)
        val prompt = LlmPromptTemplates.buildLessonSummaryPrompt(profile, messages, recentSummaries)
        val response = service.requestCompletion(LlmRequest(prompt, maxTokens = 512, temperature = 0.2))
        val summaryJson = LlmPromptTemplates.extractLessonSummaryJson(response.output)
        if (summaryJson != null) {
            learningMemoryDao.insertLessonSummary(
                LessonSummary(
                    sessionId = session.id,
                    topic = extractSummaryTopic(summaryJson),
                    summaryJson = summaryJson,
                    createdAt = Instant.now().toEpochMilli()
                )
            )
        }
        learningMemoryDao.updateSessionStatus(
            sessionId = session.id,
            status = LessonSession.STATUS_COMPLETED,
            completedAt = Instant.now().toEpochMilli()
        )
        val visibleOutput = LlmPromptTemplates.stripLessonSummaryBlock(response.output)
            .ifBlank { "Урок завершен. Сводка сохранена." }
        return LlmResponse(visibleOutput)
    }

    suspend fun initialAssistantMessage(): String {
        return if (studentProfileRepository.isOnboardingCompleted()) {
            "שלום! Чем займемся сегодня: разговорная практика, грамматика, словарь или тема из работы?"
        } else {
            LlmPromptTemplates.buildInitialOnboardingMessage()
        }
    }

    private suspend fun saveStarterLessonIfNeeded(message: String, response: LlmResponse, completedLessonCount: Int) {
        if (completedLessonCount >= STARTER_LESSON_COUNT || !LlmPromptTemplates.shouldStartStarterLesson(message)) {
            return
        }

        val lessonNumber = LlmPromptTemplates.nextStarterLessonNumber(completedLessonCount)
        progressDao.insert(
            LessonProgress(
                topic = LlmPromptTemplates.starterLessonTopic(lessonNumber),
                summary = response.output.take(120),
                createdAt = Instant.now().toEpochMilli()
            )
        )
    }

    private suspend fun getOrCreateActiveSessionId(): Long {
        val existing = learningMemoryDao.getLatestSessionByStatus()
        if (existing != null) {
            return existing.id
        }

        return learningMemoryDao.insertSession(
            LessonSession(
                createdAt = Instant.now().toEpochMilli()
            )
        )
    }

    private suspend fun saveChatMessage(sessionId: Long, text: String, isUser: Boolean) {
        learningMemoryDao.insertChatMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                text = text,
                isUser = isUser,
                createdAt = Instant.now().toEpochMilli()
            )
        )
    }

    private suspend fun loadRecentConversation(sessionId: Long): List<ChatMessage> {
        return learningMemoryDao.getRecentMessages(sessionId, CHAT_WINDOW_SIZE)
            .asReversed()
            .map { it.toChatMessage() }
    }

    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        return ChatMessage(text = text, isUser = isUser)
    }

    private fun extractSummaryTopic(summaryJson: String): String {
        return runCatching {
            JSONObject(summaryJson).optString("topic").takeIf { it.isNotBlank() }
        }.getOrNull() ?: "Урок иврита"
    }

    private companion object {
        const val CHAT_WINDOW_SIZE = 10
        const val LESSON_SUMMARY_WINDOW_SIZE = 5
        const val STARTER_LESSON_COUNT = 3
    }
}
