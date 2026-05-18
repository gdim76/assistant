package com.example.hebrewassistant.llm

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class OpenAiLlmService(
    private val apiKey: String,
    private val model: String = "gpt-4o-mini"
) : LlmService {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun requestCompletion(request: LlmRequest): LlmResponse {
        val bodyJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", request.maxTokens)
            put("temperature", request.temperature)
            put("messages", listOf(
                mapOf("role" to "user", "content" to request.prompt)
            ))
        }

        val body = bodyJson.toString().toRequestBody(jsonMediaType)
        val httpRequest = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val rawBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw LlmRequestException("LLM request failed: ${response.code} $rawBody", response.code)
            }

            val json = JSONObject(rawBody)
            val firstChoice = json.getJSONArray("choices").getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            return LlmResponse(output = message.getString("content"))
        }
    }
}
