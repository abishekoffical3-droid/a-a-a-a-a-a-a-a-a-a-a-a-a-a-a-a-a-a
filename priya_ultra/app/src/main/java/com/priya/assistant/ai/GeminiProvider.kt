package com.priya.assistant.ai

import com.priya.assistant.data.models.ChatMessage
import com.priya.assistant.data.models.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Talks to the Gemini "generateContent" REST endpoint. The model name is fully
 * user-configurable (Settings -> AI) rather than hardcoded, since Google
 * periodically deprecates model names.
 */
class GeminiProvider : AIProvider {

    override val id = "gemini"
    override val displayName = "Gemini"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun generateReply(
        conversationHistory: List<ChatMessage>,
        userMessage: String,
        systemPrompt: String,
        apiKey: String,
        model: String,
        baseUrl: String?
    ): AIResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AIResult.Error("AI provider not configured. Add a Gemini API key in Settings.")
        }
        val effectiveModel = model.ifBlank { "gemini-2.0-flash" }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModel:generateContent?key=$apiKey"

        val contents = JSONArray()
        conversationHistory.forEach { msg ->
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            contents.put(
                JSONObject().put("role", role).put(
                    "parts", JSONArray().put(JSONObject().put("text", msg.text))
                )
            )
        }
        contents.put(
            JSONObject().put("role", "user").put(
                "parts", JSONArray().put(JSONObject().put("text", userMessage))
            )
        )

        val body = JSONObject().apply {
            put("contents", contents)
            put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            )
            put(
                "generationConfig",
                JSONObject().put("temperature", 0.9).put("maxOutputTokens", 800)
            )
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext AIResult.Error(AIErrorMapper.fromHttpCode(response.code, displayName))
                }
                val json = JSONObject(responseBody)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (text.isNullOrBlank()) {
                    AIResult.Error("Gemini returned an empty response.")
                } else {
                    AIResult.Success(text.trim())
                }
            }
        } catch (e: Exception) {
            AIResult.Error(AIErrorMapper.fromException(e), e)
        }
    }

    override suspend fun testConnection(apiKey: String, model: String, baseUrl: String?): AIResult {
        return generateReply(
            conversationHistory = emptyList(),
            userMessage = "Say 'hello' in one short word.",
            systemPrompt = "You are a connection test. Reply with a single short word.",
            apiKey = apiKey,
            model = model
        )
    }
}
