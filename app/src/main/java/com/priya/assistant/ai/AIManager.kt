package com.priya.assistant.ai

import com.priya.assistant.data.SettingsRepository
import com.priya.assistant.data.models.ChatMessage
import com.priya.assistant.security.SecureKeyManager

/**
 * Central entry point the rest of the app talks to. Owns the provider registry,
 * resolves the currently-selected provider + its encrypted key, and injects the
 * PRIYA personality/system prompt. If no key is configured it returns a friendly
 * "not configured" error rather than throwing — the app must stay usable offline.
 */
class AIManager(
    private val secureKeyManager: SecureKeyManager,
    private val settingsRepository: SettingsRepository
) {
    private val providers: Map<String, AIProvider> = listOf(
        GeminiProvider(),
        OpenAIProvider(),
        CustomOpenAIProvider()
    ).associateBy { it.id }

    fun availableProviders(): List<AIProvider> = providers.values.toList()

    fun isConfigured(): Boolean {
        val settings = settingsRepository.currentAISettingsBlocking()
        return secureKeyManager.hasKey(settings.providerId)
    }

    suspend fun sendMessage(
        conversationHistory: List<ChatMessage>,
        userMessage: String,
        actionsSystemPromptAddendum: String = ""
    ): AIResult {
        val settings = settingsRepository.currentAISettingsBlocking()
        val provider = providers[settings.providerId] ?: providers.getValue("gemini")
        val apiKey = secureKeyManager.getKey(settings.providerId).orEmpty()

        if (apiKey.isBlank()) {
            return AIResult.Error("AI provider not configured. Add an API key in Settings \u2192 AI.")
        }
        if (settings.providerId == "custom" && settings.customBaseUrl.isBlank()) {
            return AIResult.Error("Set a base URL for your custom provider in Settings \u2192 AI.")
        }

        return provider.generateReply(
            conversationHistory = conversationHistory,
            userMessage = userMessage,
            systemPrompt = PriyaPersonality.systemPrompt(actionsSystemPromptAddendum),
            apiKey = apiKey,
            model = settings.model.ifBlank { defaultModelFor(settings.providerId) },
            baseUrl = settings.customBaseUrl
        )
    }

    suspend fun testConnection(providerId: String, apiKey: String, model: String, baseUrl: String): AIResult {
        val provider = providers[providerId] ?: return AIResult.Error("Unknown provider.")
        if (apiKey.isBlank()) return AIResult.Error("Enter an API key first.")
        return provider.testConnection(apiKey, model, baseUrl)
    }

    private fun defaultModelFor(providerId: String): String = when (providerId) {
        "gemini" -> "gemini-2.0-flash"
        "openai" -> "gpt-4o-mini"
        else -> ""
    }
}
