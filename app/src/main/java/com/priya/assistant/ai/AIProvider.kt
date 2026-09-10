package com.priya.assistant.ai

import com.priya.assistant.data.models.ChatMessage

sealed class AIResult {
    data class Success(val reply: String) : AIResult()
    data class Error(val friendlyMessage: String, val cause: Throwable? = null) : AIResult()
}

interface AIProvider {
    val id: String
    val displayName: String

    suspend fun generateReply(
        conversationHistory: List<ChatMessage>,
        userMessage: String,
        systemPrompt: String,
        apiKey: String,
        model: String,
        baseUrl: String? = null
    ): AIResult

    suspend fun testConnection(apiKey: String, model: String, baseUrl: String? = null): AIResult
}

/** Maps raw HTTP/network failures into the friendly messages required by the spec. */
object AIErrorMapper {
    fun fromHttpCode(code: Int, providerName: String): String = when (code) {
        401, 403 -> "Your API key appears to be invalid."
        429 -> "Your AI provider's quota is currently unavailable. You can switch providers in Settings."
        in 500..599 -> "$providerName is having trouble right now. Please try again shortly."
        else -> "$providerName returned an unexpected error (code $code)."
    }

    fun fromException(e: Throwable): String = when (e) {
        is java.net.UnknownHostException, is java.net.ConnectException -> "I can't reach the AI service right now."
        is java.net.SocketTimeoutException -> "The AI service took too long to respond."
        else -> "Something went wrong talking to the AI service."
    }
}
