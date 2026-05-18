package com.example.hebrewassistant.llm

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
}
