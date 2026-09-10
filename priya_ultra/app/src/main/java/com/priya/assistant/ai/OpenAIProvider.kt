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

open class OpenAIProvider(
    override val id: String = "openai",
    override val displayName: String = "OpenAI",
    private val defaultBaseUrl: String = "https://api.openai.com/v1"
) : AIProvider {

    protected val client: OkHttpClient = OkHttpClient.Builder()
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
            return@withContext AIResult.Error("AI provider not configured. Add an API key in Settings.")
        }
        val effectiveModel = model.ifBlank { "gpt-4o-mini" }
        val effectiveBaseUrl = (baseUrl?.takeIf { it.isNotBlank() } ?: defaultBaseUrl).trimEnd('/')

        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        conversationHistory.forEach { msg ->
            val role = if (msg.sender == MessageSender.USER) "user" else "assistant"
            messages.put(JSONObject().put("role", role).put("content", msg.text))
        }
        messages.put(JSONObject().put("role", "user").put("content", userMessage))

        val body = JSONObject().apply {
            put("model", effectiveModel)
            put("messages", messages)
            put("temperature", 0.9)
            put("max_tokens", 800)
        }

        try {
            val request = Request.Builder()
                .url("$effectiveBaseUrl/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext AIResult.Error(AIErrorMapper.fromHttpCode(response.code, displayName))
                }
                val json = JSONObject(responseBody)
                val text = json.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")

                if (text.isNullOrBlank()) {
                    AIResult.Error("$displayName returned an empty response.")
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
            model = model,
            baseUrl = baseUrl
        )
    }
}
