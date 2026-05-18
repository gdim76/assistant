package com.example.hebrewassistant.llm

import com.example.hebrewassistant.data.LessonProgress

object LlmPromptTemplates {
    fun buildLessonPrompt(topic: String): String {
        return "Ты — виртуальный репетитор по ивриту. Составь адаптивный урок для начинающего студента на тему: '$topic'. " +
            "Включи краткое объяснение грамматики, полезные фразы и упражнения с переводом."
    }

    fun buildReviewPrompt(progress: List<LessonProgress>): String {
        if (progress.isEmpty()) {
            return "Пользователь только начинает. Сформируй вводное упражнение по базовой лексике иврита."
        }

        val recentTopics = progress.take(3).joinToString(separator = ", ") { it.topic }
        return "У студента были уроки по темам: $recentTopics. " +
            "Предложи новое задание, которое повторит предыдущие темы и добавит одно новое слово."
    }

    fun buildChatPrompt(message: String): String {
        return "Ты — помощник по изучению иврита. Ответь пользователю на сообщение: '$message'. " +
            "Если пользователь говорит на иврите, исправь его ошибки, если они есть, и ответь на иврите с переводом на русский. " +
            "Если пользователь спрашивает на русском, ответь на русском и предложи полезные фразы на иврите."
    }
}
