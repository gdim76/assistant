package com.example.hebrewassistant.llm

interface LlmService {
    suspend fun requestCompletion(request: LlmRequest): LlmResponse
}

data class LlmRequest(
    val prompt: String,
    val maxTokens: Int = 256,
    val temperature: Double = 0.8
)

data class LlmResponse(
    val output: String
)

class LlmRequestException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class MockLlmService : LlmService {
    override suspend fun requestCompletion(request: LlmRequest): LlmResponse {
        return LlmResponse(output = buildMockAnswer(request.prompt))
    }

    private fun buildMockAnswer(prompt: String): String {
        return when {
            prompt.contains("первый чат-сценарий") -> buildMockOnboardingAnswer(prompt)
            prompt.contains("<lesson_summary>") -> buildMockLessonSummaryAnswer()
            prompt.contains("Проведи стартовый урок") -> buildMockStarterLessonAnswer(prompt)
            prompt.contains("Составь адаптивный урок на тему") -> buildMockLessonAnswer(prompt)
            else -> buildMockChatAnswer(prompt)
        }
    }

    private fun buildMockOnboardingAnswer(prompt: String): String {
        val message = extractQuotedValue(prompt, "Сообщение ученика")
        val userAnswerCount = Regex("(?m)^Ученик:").findAll(prompt).count()
        val name = Regex("(?i)(?:my name is|меня зовут|я)\\s+([A-Za-zА-Яа-яЁё-]+)")
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?: message.trim().takeIf { it.isNotBlank() && userAnswerCount == 1 }
            ?: "тебя"

        return when (userAnswerCount) {
            0 -> "Как тебя зовут?"
            1 -> """
                Приятно познакомиться, $name.

                Чем ты занимаешься?
            """.trimIndent()
            2 -> """
                Понял.

                Какие темы тебе интересны? Например: история, литература, технологии, работа, путешествия.
            """.trimIndent()
            3 -> """
                Отлично, будем использовать эти темы в примерах.

                Зачем тебе иврит: работа, жизнь в Израиле, чтение, общение, экзамен или что-то другое?
            """.trimIndent()
            4 -> """
                Спасибо.

                Какой у тебя родной язык или языки?
            """.trimIndent()
            else -> """
                Начнем короткую диагностику. Я буду задавать вопросы по одному.

                שאלה 1:
                מה שלומך היום?

                Ответь на иврите одной простой фразой.
            """.trimIndent()
        }
    }

    private fun buildMockLessonSummaryAnswer(): String {
        return """
            Урок завершен. Я сохранил краткую сводку прогресса.
            <lesson_summary>{
              "topic": "Практика иврита",
              "newWords": ["שלום", "קוראים לי"],
              "practicedPhrases": ["קוראים לי...", "אני לומד עברית"],
              "repeatedErrors": [],
              "correctedErrors": [],
              "progressScore": 6,
              "nextLessonRecommendation": "Закрепить фразы знакомства и добавить אני גר/גרה.",
              "profilePatch": {
                "currentLevel": "A1",
                "weakPointsToAdd": [],
                "weakPointsToRemove": [],
                "masteredTopicsToAdd": ["basic greetings"],
                "teacherNotes": "Ученик начал практиковать короткие фразы знакомства."
              }
            }</lesson_summary>
        """.trimIndent()
    }

    private fun buildMockStarterLessonAnswer(prompt: String): String {
        val topic = Regex("Проведи стартовый урок \\d+ из \\d+: '([^']*)'")
            .find(prompt)
            ?.groupValues
            ?.getOrNull(1)
            ?: "Знакомство на иврите"

        return """
            Урок: $topic

            1. Фразы
            - שלום - привет
            - קוראים לי... - меня зовут...
            - אני לומד עברית - я учу иврит

            2. Мини-диалог
            א: שלום, איך קוראים לך?
            ב: קוראים לי דימה.

            Теперь твоя очередь: напиши на иврите "Привет, меня зовут ...".
        """.trimIndent()
    }

    private fun buildMockLessonAnswer(prompt: String): String {
        val topic = extractQuotedValue(prompt, "Составь адаптивный урок на тему")
            .ifBlank { "повседневный разговор" }

        return """
            Мини-урок по теме: $topic

            1. Фразы
            - שלום - привет
            - תודה - спасибо
            - בבקשה - пожалуйста

            2. Практика
            Переведи на иврит: "Привет, спасибо".

            3. Мини-диалог
            А: שלום, מה שלומך?
            Б: טוב, תודה.
        """.trimIndent()
    }

    private fun buildMockChatAnswer(prompt: String): String {
        val message = extractQuotedValue(prompt, "Ответь пользователю на сообщение")
        return """
            Я понял: "$message".

            На иврите можно сказать:
            אני רוצה ללמוד עברית.

            Перевод: я хочу учить иврит.
        """.trimIndent()
    }

    private fun extractQuotedValue(prompt: String, label: String): String {
        val index = prompt.indexOf(label)
        if (index == -1) return ""
        val tail = prompt.substring(index)
        return Regex("'([^']*)'")
            .find(tail)
            ?.groupValues
            ?.getOrNull(1)
            .orEmpty()
    }
}
