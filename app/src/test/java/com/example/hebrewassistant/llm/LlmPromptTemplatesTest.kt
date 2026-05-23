package com.example.hebrewassistant.llm

import com.example.hebrewassistant.data.ChatMessage
import com.example.hebrewassistant.data.LessonProgress
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmPromptTemplatesTest {

    @Test
    fun `buildLessonPrompt includes topic and task description`() {
        val topic = "приветствие"
        val prompt = LlmPromptTemplates.buildLessonPrompt(topic)

        assertTrue("Prompt should include the topic", prompt.contains(topic))
        assertTrue("Prompt should mention the tutor role", prompt.contains("виртуальный репетитор"))
    }

    @Test
    fun `buildReviewPrompt suggests review when history exists`() {
        val progress = listOf(
            LessonProgress(topic = "алфавит", summary = "текст", createdAt = 1),
            LessonProgress(topic = "грамматика", summary = "текст", createdAt = 2)
        )
        val prompt = LlmPromptTemplates.buildReviewPrompt(progress)

        assertTrue("Prompt should mention recent topics", prompt.contains("алфавит"))
        assertTrue("Prompt should mention recent topics", prompt.contains("грамматика"))
    }

    @Test
    fun `buildReviewPrompt returns starter prompt when progress is empty`() {
        val prompt = LlmPromptTemplates.buildReviewPrompt(emptyList())

        assertTrue("Prompt should return starter prompt for empty progress", prompt.contains("Пользователь только начинает"))
    }

    @Test
    fun `buildOnboardingPrompt includes structured profile contract`() {
        val prompt = LlmPromptTemplates.buildOnboardingPrompt("Меня зовут Дима", profile = null)

        assertTrue("Prompt should include onboarding scenario", prompt.contains("первый чат-сценарий"))
        assertTrue("Prompt should require one question at a time", prompt.contains("только один главный вопрос за раз"))
        assertTrue("Prompt should include profile marker", prompt.contains("<student_profile>"))
        assertTrue("Prompt should request placement test", prompt.contains("10-15 вопросов"))
    }

    @Test
    fun `buildInitialOnboardingMessage starts with one question`() {
        val message = LlmPromptTemplates.buildInitialOnboardingMessage()

        assertTrue("Initial message should ask for name", message.contains("Как тебя зовут?"))
        assertTrue("Initial message should not ask about work immediately", !message.contains("чем ты занимаешься"))
    }

    @Test
    fun `buildStarterLessonPrompt defines first lesson scenario`() {
        val prompt = LlmPromptTemplates.buildStarterLessonPrompt(
            lessonNumber = 1,
            profile = null,
            conversation = emptyList()
        )

        assertTrue("Prompt should include first lesson topic", prompt.contains("Знакомство на иврите"))
        assertTrue("Prompt should include Hebrew introduction phrase", prompt.contains("קוראים לי"))
        assertTrue("Prompt should require one exercise", prompt.contains("одно упражнение"))
    }

    @Test
    fun `starter lesson helper selects first three lessons`() {
        assertTrue("First lesson should be selected for empty progress", LlmPromptTemplates.nextStarterLessonNumber(0) == 1)
        assertTrue("Second lesson should be selected after one lesson", LlmPromptTemplates.nextStarterLessonNumber(1) == 2)
        assertTrue("Third lesson should be selected after two lessons", LlmPromptTemplates.nextStarterLessonNumber(2) == 3)
        assertTrue("Lesson number should not exceed starter plan", LlmPromptTemplates.nextStarterLessonNumber(10) == 3)
    }

    @Test
    fun `shouldStartStarterLesson detects start intent`() {
        assertTrue("Start intent should be detected", LlmPromptTemplates.shouldStartStarterLesson("давай начнем урок"))
        assertTrue("Generic chat should not start lesson", !LlmPromptTemplates.shouldStartStarterLesson("как сказать спасибо?"))
    }

    @Test
    fun `buildLessonSummaryPrompt asks for structured summary`() {
        val prompt = LlmPromptTemplates.buildLessonSummaryPrompt(
            profile = null,
            lessonMessages = listOf(
                ChatMessage("Как тебя зовут?", isUser = false),
                ChatMessage("קוראים לי דימה", isUser = true)
            ),
            lessonSummaries = emptyList()
        )

        assertTrue("Prompt should identify learning analyst role", prompt.contains("аналитик данных обучения"))
        assertTrue("Prompt should include lesson summary marker", prompt.contains("<lesson_summary>"))
        assertTrue("Prompt should request profile patch", prompt.contains("profilePatch"))
    }

    @Test
    fun `extractLessonSummaryJson returns json and strip removes marker`() {
        val response = "Сводка готова.\n<lesson_summary>{\"topic\":\"Знакомство\"}</lesson_summary>"

        val json = LlmPromptTemplates.extractLessonSummaryJson(response)
        val stripped = LlmPromptTemplates.stripLessonSummaryBlock(response)

        assertTrue("JSON should be extracted", json == "{\"topic\":\"Знакомство\"}")
        assertTrue("Marker should be removed", !stripped.contains("<lesson_summary>"))
        assertTrue("Visible response should remain", stripped == "Сводка готова.")
    }

    @Test
    fun `buildOnboardingPrompt includes recent conversation`() {
        val prompt = LlmPromptTemplates.buildOnboardingPrompt(
            message = "Я девопс инженер",
            profile = null,
            conversation = listOf(
                ChatMessage("Как тебя зовут?", isUser = false),
                ChatMessage("Меня зовут Дима", isUser = true)
            )
        )

        assertTrue("Prompt should include assistant history", prompt.contains("Ассистент: Как тебя зовут?"))
        assertTrue("Prompt should include user history", prompt.contains("Ученик: Меня зовут Дима"))
    }

    @Test
    fun `extractStudentProfileJson returns json and strip removes marker`() {
        val response = "Готово.\n<student_profile>{\"name\":\"Дима\"}</student_profile>"

        val json = LlmPromptTemplates.extractStudentProfileJson(response)
        val stripped = LlmPromptTemplates.stripStudentProfileBlock(response)

        assertTrue("JSON should be extracted", json == "{\"name\":\"Дима\"}")
        assertTrue("Marker should be removed", !stripped.contains("<student_profile>"))
        assertTrue("Visible response should remain", stripped == "Готово.")
    }
}
