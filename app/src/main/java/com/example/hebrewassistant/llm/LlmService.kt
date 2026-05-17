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
        val answer = "Урок по ивриту для темы: ${request.prompt.take(80)}...\n" +
            "1. Приветствие и основная лексика.\n" +
            "2. Простые фразы для диалога.\n" +
            "3. Практическое упражнение с переводом."
        return LlmResponse(output = answer)
    }
}
