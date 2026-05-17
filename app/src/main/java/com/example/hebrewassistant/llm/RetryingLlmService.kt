package com.example.hebrewassistant.llm

import kotlinx.coroutines.delay
import java.io.IOException

class RetryingLlmService(
    private val delegate: LlmService,
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 500,
    private val maxDelayMs: Long = 5000
) : LlmService {
    override suspend fun requestCompletion(request: LlmRequest): LlmResponse {
        var attempt = 0
        var delayMs = initialDelayMs

        while (true) {
            try {
                return delegate.requestCompletion(request)
            } catch (e: Exception) {
                if (!shouldRetry(e) || attempt >= maxRetries) {
                    throw e
                }
                attempt++
                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
            }
        }
    }

    private fun shouldRetry(exception: Exception): Boolean {
        return when (exception) {
            is IOException -> true
            is LlmRequestException -> exception.statusCode?.let { it == 429 || it in 500..599 } ?: false
            else -> false
        }
    }
}
