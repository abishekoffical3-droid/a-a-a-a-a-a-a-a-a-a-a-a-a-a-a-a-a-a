package com.priya.assistant.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.priya.assistant.data.models.AISettings
import com.priya.assistant.data.models.SpeechLanguage
import com.priya.assistant.data.models.VoiceEngine
import com.priya.assistant.data.models.VoiceSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "priya_settings")

/**
 * Holds every user-configurable, non-secret preference: AI provider/model choice,
 * voice settings, language selection. API keys themselves live in SecureKeyManager.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val PROVIDER_ID = stringPreferencesKey("ai_provider_id")
        val MODEL = stringPreferencesKey("ai_model")
        val CUSTOM_BASE_URL = stringPreferencesKey("ai_custom_base_url")

        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val CONTINUOUS_LISTENING = booleanPreferencesKey("continuous_listening")
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak")
        val LANGUAGE = stringPreferencesKey("speech_language")
        val SPEECH_SPEED = floatPreferencesKey("speech_speed")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val VOICE_ENGINE = stringPreferencesKey("voice_engine")
        val EXTERNAL_TTS_URL = stringPreferencesKey("external_tts_base_url")
        val EXTERNAL_TTS_VOICE = stringPreferencesKey("external_tts_voice_id")
    }

    val aiSettingsFlow: Flow<AISettings> = context.dataStore.data.map { prefs ->
        AISettings(
            providerId = prefs[Keys.PROVIDER_ID] ?: "gemini",
            model = prefs[Keys.MODEL] ?: "",
            customBaseUrl = prefs[Keys.CUSTOM_BASE_URL] ?: ""
        )
    }

    val voiceSettingsFlow: Flow<VoiceSettings> = context.dataStore.data.map { prefs ->
        VoiceSettings(
            voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: true,
            continuousListening = prefs[Keys.CONTINUOUS_LISTENING] ?: false,
            autoSpeak = prefs[Keys.AUTO_SPEAK] ?: true,
            language = SpeechLanguage.entries.find { it.tag == prefs[Keys.LANGUAGE] } ?: SpeechLanguage.AUTO,
            speechSpeed = prefs[Keys.SPEECH_SPEED] ?: 1.0f,
            speechPitch = prefs[Keys.SPEECH_PITCH] ?: 1.0f,
            voiceEngine = VoiceEngine.entries.find { it.name == prefs[Keys.VOICE_ENGINE] } ?: VoiceEngine.AUTO,
            externalTtsBaseUrl = prefs[Keys.EXTERNAL_TTS_URL] ?: "",
            externalTtsVoiceId = prefs[Keys.EXTERNAL_TTS_VOICE] ?: ""
        )
    }

    suspend fun updateAISettings(settings: AISettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PROVIDER_ID] = settings.providerId
            prefs[Keys.MODEL] = settings.model
            prefs[Keys.CUSTOM_BASE_URL] = settings.customBaseUrl
        }
    }

    suspend fun updateVoiceSettings(settings: VoiceSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VOICE_ENABLED] = settings.voiceEnabled
            prefs[Keys.CONTINUOUS_LISTENING] = settings.continuousListening
            prefs[Keys.AUTO_SPEAK] = settings.autoSpeak
            prefs[Keys.LANGUAGE] = settings.language.tag
            prefs[Keys.SPEECH_SPEED] = settings.speechSpeed
            prefs[Keys.SPEECH_PITCH] = settings.speechPitch
            prefs[Keys.VOICE_ENGINE] = settings.voiceEngine.name
            prefs[Keys.EXTERNAL_TTS_URL] = settings.externalTtsBaseUrl
            prefs[Keys.EXTERNAL_TTS_VOICE] = settings.externalTtsVoiceId
        }
    }

    /** Synchronous snapshot for call sites that aren't already coroutine-scoped (e.g. AccessibilityService). */
    fun currentVoiceSettingsBlocking(): VoiceSettings = runBlocking { voiceSettingsFlow.first() }

    fun currentAISettingsBlocking(): AISettings = runBlocking { aiSettingsFlow.first() }
}
