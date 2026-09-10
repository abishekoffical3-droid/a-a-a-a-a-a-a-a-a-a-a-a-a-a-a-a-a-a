package com.priya.assistant.data.models

enum class MessageSender { USER, PRIYA }

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long
)

data class Conversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
    val createdAt: Long,
    val updatedAt: Long
)

enum class SpeechLanguage(val tag: String, val label: String) {
    AUTO("auto", "Auto"),
    NEPALI("ne-NP", "Nepali"),
    HINDI("hi-IN", "Hindi"),
    ENGLISH("en-US", "English")
}

enum class VoiceEngine { ANDROID, EXTERNAL, AUTO }

data class VoiceSettings(
    val voiceEnabled: Boolean = true,
    val continuousListening: Boolean = false,
    val autoSpeak: Boolean = true,
    val language: SpeechLanguage = SpeechLanguage.AUTO,
    val speechSpeed: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val voiceEngine: VoiceEngine = VoiceEngine.AUTO,
    val externalTtsBaseUrl: String = "",
    val externalTtsVoiceId: String = ""
)

data class AISettings(
    val providerId: String = "gemini",
    val model: String = "",
    val customBaseUrl: String = ""
)
