package com.example.hebrewassistant.llm

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GeminiLlmService(private val apiKey: String) : LlmService {
    private val client = OkHttpClient()
    private val url = "https://gemini.googleapis.com/v1/models/gemini-1.5-mini:generate"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun requestCompletion(request: LlmRequest): LlmResponse {
        val bodyJson = JSONObject().apply {
            put("prompt", JSONObject().put("text", request.prompt))
            put("temperature", request.temperature)
            put("maxOutputTokens", request.maxTokens)
        }

        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val rawBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw LlmRequestException("Gemini request failed: ${response.code} $rawBody", response.code)
            }
            val json = JSONObject(rawBody)
            val candidate = json
                .getJSONArray("candidates")
                .getJSONObject(0)
            return LlmResponse(output = candidate.optString("content", candidate.optString("output", "")))
        }
    }
}
