package com.example.hebrewassistant.llm

import com.example.hebrewassistant.data.LessonProgress
import com.example.hebrewassistant.data.StudentProfile

object LlmPromptTemplates {
    fun buildLessonPrompt(topic: String): String {
        return buildLessonPrompt(topic, profile = null)
    }

    fun buildLessonPrompt(topic: String, profile: StudentProfile?): String {
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            "Составь адаптивный урок на тему: '$topic'. " +
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
        return buildTeacherSystemPrompt(profile = null) + "\n\n" +
            "Ответь пользователю на сообщение: '$message'. " +
            "Если пользователь говорит на иврите, исправь его ошибки, если они есть, и ответь на иврите с переводом на русский. " +
            "Если пользователь спрашивает на русском, ответь на русском и предложи полезные фразы на иврите."
    }

    fun buildChatPrompt(message: String, profile: StudentProfile?): String {
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            "Ответь пользователю на сообщение: '$message'. " +
            "Если пользователь говорит на иврите, исправь его ошибки, если они есть, и ответь на иврите с переводом на русский. " +
            "Если пользователь спрашивает на русском, ответь на русском и предложи полезные фразы на иврите. " +
            "Используй профиль ученика для выбора примеров, темпа и сложности."
    }

    fun buildInitialOnboardingMessage(): String {
        return "שלום! Я буду твоим преподавателем иврита. Давай начнем со знакомства: как тебя зовут, чем ты занимаешься, какие темы тебе интересны и зачем тебе иврит? После этого я проведу короткую диагностику на иврите с постепенным ростом сложности."
    }

    fun buildOnboardingPrompt(message: String, profile: StudentProfile?): String {
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            "Сейчас идет первый чат-сценарий знакомства и диагностики уровня. " +
            "Твоя задача: выяснить имя ученика, родной язык, интересы, область работы и цель изучения иврита. " +
            "Затем проведи тест на 10-15 вопросов на иврите с постепенным ростом сложности от уровня 1 до уровня 4. " +
            "Не задавай все вопросы сразу: веди диалог естественно, небольшими порциями. " +
            "После каждого ответа кратко реагируй, исправляй ошибки и выбирай следующий вопрос. " +
            "Когда данных достаточно, заверши диагностику и в самом конце ответа добавь один служебный блок в точном формате:\n" +
            "<student_profile>{\"name\":\"...\",\"nativeLanguage\":\"...\",\"interests\":\"...\",\"workArea\":\"...\",\"learningGoal\":\"...\",\"currentLevel\":\"...\",\"placementSummary\":\"...\"}</student_profile>\n" +
            "Не добавляй служебный блок, пока диагностика не завершена. " +
            "Сообщение ученика: '$message'."
    }

    fun extractStudentProfileJson(response: String): String? {
        return Regex("<student_profile>(.*?)</student_profile>", RegexOption.DOT_MATCHES_ALL)
            .find(response)
            ?.groupValues
            ?.get(1)
            ?.trim()
    }

    fun stripStudentProfileBlock(response: String): String {
        return response.replace(
            Regex("\\s*<student_profile>.*?</student_profile>", RegexOption.DOT_MATCHES_ALL),
            ""
        ).trim()
    }

    private fun buildTeacherSystemPrompt(profile: StudentProfile?): String {
        val profileContext = profile?.let {
            listOfNotNull(
                it.name?.let { value -> "Имя: $value" },
                it.nativeLanguage?.let { value -> "Родной язык: $value" },
                it.interests?.let { value -> "Интересы: $value" },
                it.workArea?.let { value -> "Работа/область: $value" },
                it.learningGoal?.let { value -> "Цель изучения: $value" },
                it.currentLevel?.let { value -> "Текущий уровень: $value" },
                it.placementSummary?.let { value -> "Резюме диагностики: $value" }
            ).joinToString(separator = "\n")
        }?.takeIf { it.isNotBlank() } ?: "Профиль ученика еще не заполнен."

        return """
            Ты — виртуальный репетитор по ивриту и творческий преподаватель языка.
            Ты адаптируешь уроки под ученика, объясняешь понятно и даешь практику маленькими шагами.
            Используй разные форматы: диалоги, перевод, исправление ошибок, мини-тесты, словарные карточки, грамматику в контексте, вопросы на понимание и короткие творческие задания.
            Подключай литературу, историю, культуру Израиля, современную речь и ИТ-темы, когда это помогает сделать урок живым и полезным.
            Не выдумывай слабые места и любимые форматы ученика заранее: наблюдай за ответами и мягко усиливай практику в процессе.
            Профиль ученика:
            $profileContext
        """.trimIndent()
    }
}
