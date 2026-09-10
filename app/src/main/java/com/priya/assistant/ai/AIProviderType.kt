package com.priya.assistant.ai

enum class AIProviderType(val id: String, val displayName: String, val defaultModel: String) {
    GEMINI("gemini", "Gemini", "gemini-2.0-flash"),
    OPENAI("openai", "OpenAI", "gpt-4o-mini"),
    CUSTOM("custom", "Custom OpenAI-compatible", "");

    companion object {
        fun fromId(id: String): AIProviderType = entries.find { it.id == id } ?: GEMINI
    }
}
