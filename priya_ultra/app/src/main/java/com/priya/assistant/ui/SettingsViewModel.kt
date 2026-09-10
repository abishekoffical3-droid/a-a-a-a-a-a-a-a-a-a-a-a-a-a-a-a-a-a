package com.priya.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priya.assistant.PriyaApplication
import com.priya.assistant.ai.AIProviderType
import com.priya.assistant.ai.AIResult
import com.priya.assistant.data.models.AISettings
import com.priya.assistant.data.models.SpeechLanguage
import com.priya.assistant.data.models.VoiceEngine
import com.priya.assistant.data.models.VoiceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val app: PriyaApplication) : ViewModel() {

    val aiSettings: StateFlow<AISettings> = app.settingsRepository.aiSettingsFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, AISettings())

    val voiceSettings: StateFlow<VoiceSettings> = app.settingsRepository.voiceSettingsFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, VoiceSettings())

    private val _testResult = MutableStateFlow<TestResult?>(null)
    val testResult: StateFlow<TestResult?> = _testResult.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    sealed class TestResult {
        data class Ok(val message: String) : TestResult()
        data class Failed(val message: String) : TestResult()
    }

    fun availableProviders() = AIProviderType.entries.toList()

    fun maskedKeyFor(providerId: String): String? = app.secureKeyManager.maskedKey(providerId)

    fun hasKeyFor(providerId: String): Boolean = app.secureKeyManager.hasKey(providerId)

    fun selectProvider(providerId: String) {
        viewModelScope.launch {
            app.settingsRepository.updateAISettings(aiSettings.value.copy(providerId = providerId))
        }
    }

    fun updateModel(model: String) {
        viewModelScope.launch {
            app.settingsRepository.updateAISettings(aiSettings.value.copy(model = model))
        }
    }

    fun updateCustomBaseUrl(url: String) {
        viewModelScope.launch {
            app.settingsRepository.updateAISettings(aiSettings.value.copy(customBaseUrl = url))
        }
    }

    fun saveApiKey(providerId: String, key: String) {
        app.secureKeyManager.saveKey(providerId, key)
    }

    fun deleteApiKey(providerId: String) {
        app.secureKeyManager.deleteKey(providerId)
    }

    fun testConnection(providerId: String, apiKey: String, model: String, baseUrl: String) {
        viewModelScope.launch {
            _isTesting.value = true
            _testResult.value = null
            val result = app.aiManager.testConnection(providerId, apiKey, model, baseUrl)
            _testResult.value = when (result) {
                is AIResult.Success -> TestResult.Ok("Connected successfully.")
                is AIResult.Error -> TestResult.Failed(result.friendlyMessage)
            }
            _isTesting.value = false
        }
    }

    fun clearTestResult() {
        _testResult.value = null
    }

    fun updateVoiceEnabled(enabled: Boolean) = updateVoice { it.copy(voiceEnabled = enabled) }
    fun updateContinuousListening(enabled: Boolean) = updateVoice { it.copy(continuousListening = enabled) }
    fun updateAutoSpeak(enabled: Boolean) = updateVoice { it.copy(autoSpeak = enabled) }
    fun updateLanguage(language: SpeechLanguage) = updateVoice { it.copy(language = language) }
    fun updateSpeechSpeed(speed: Float) = updateVoice { it.copy(speechSpeed = speed) }
    fun updateSpeechPitch(pitch: Float) = updateVoice { it.copy(speechPitch = pitch) }
    fun updateVoiceEngine(engine: VoiceEngine) = updateVoice { it.copy(voiceEngine = engine) }
    fun updateExternalTtsUrl(url: String) = updateVoice { it.copy(externalTtsBaseUrl = url) }
    fun updateExternalTtsVoiceId(id: String) = updateVoice { it.copy(externalTtsVoiceId = id) }

    fun testVoice() {
        app.voiceManager.speak("Hello, I'm PRIYA. This is how I'll sound.")
    }

    private inline fun updateVoice(crossinline transform: (VoiceSettings) -> VoiceSettings) {
        viewModelScope.launch {
            app.settingsRepository.updateVoiceSettings(transform(voiceSettings.value))
        }
    }

    companion object {
        fun factory(app: PriyaApplication) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(app) as T
        }
    }
}
