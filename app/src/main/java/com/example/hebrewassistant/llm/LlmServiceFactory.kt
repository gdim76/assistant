package com.example.hebrewassistant.llm

class LlmServiceFactory {
    fun create(provider: LlmProvider, apiKey: String): LlmService {
        if (apiKey.isBlank()) {
            return MockLlmService()
        }

        return when (provider) {
            LlmProvider.GEMINI -> GeminiLlmService(apiKey)
            LlmProvider.OPENAI -> OpenAiLlmService(apiKey)
        }
    }
}
