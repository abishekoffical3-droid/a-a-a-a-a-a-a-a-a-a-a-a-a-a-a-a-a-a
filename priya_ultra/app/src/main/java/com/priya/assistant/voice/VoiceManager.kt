package com.priya.assistant.voice

import android.content.Context
import com.priya.assistant.data.SettingsRepository
import com.priya.assistant.data.models.VoiceSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Single owner of the voice pipeline. Screens observe [state] and [lastError];
 * ViewModels call [startListening]/[stopListening]/[speak]. Ensures PRIYA never
 * listens to her own voice: speaking always pauses recognition (spec §6).
 */
class VoiceManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow<String?>(null)
    val lastRecognizedText: StateFlow<String?> = _lastRecognizedText.asStateFlow()

    private var currentVoiceSettings: VoiceSettings = VoiceSettings()

    private val recognizerManager = SpeechRecognizerManager(
        context = context,
        onPartialState = { newState ->
            // Never let the recognizer downgrade us out of an active SPEAKING/PROCESSING state.
            if (_state.value != VoiceState.SPEAKING) _state.value = newState
        },
        onResult = { text ->
            _lastRecognizedText.value = text
            _state.value = VoiceState.PROCESSING
        },
        onError = { message ->
            _lastError.value = message
            _state.value = VoiceState.ERROR
        }
    )

    private val ttsManager = TextToSpeechManager(
        context = context,
        onStart = {
            _state.value = VoiceState.SPEAKING
            recognizerManager.stopListening()
        },
        onDone = {
            _state.value = VoiceState.IDLE
            if (currentVoiceSettings.continuousListening) {
                recognizerManager.startListening()
            }
        },
        onFallbackNotice = { message -> _lastError.value = message }
    )

    init {
        scope.launch {
            settingsRepository.voiceSettingsFlow.collect { settings ->
                val continuousModeChanged = currentVoiceSettings.continuousListening != settings.continuousListening
                currentVoiceSettings = settings
                recognizerManager.setContinuousMode(settings.continuousListening)
                recognizerManager.setLanguage(settings.language)

                if (continuousModeChanged) {
                    if (settings.continuousListening) {
                        PriyaVoiceService.start(context)
                        startListening()
                    } else {
                        PriyaVoiceService.stop(context)
                    }
                }
            }
        }
    }

    fun isRecognitionAvailable(): Boolean = recognizerManager.isAvailable()

    fun startListening() {
        if (_state.value == VoiceState.SPEAKING) return
        _lastError.value = null
        recognizerManager.startListening()
    }

    fun stopListening() {
        recognizerManager.stopListening()
        if (_state.value == VoiceState.LISTENING) _state.value = VoiceState.IDLE
    }

    fun setExecuting() {
        _state.value = VoiceState.EXECUTING
    }

    fun setIdle() {
        _state.value = VoiceState.IDLE
    }

    fun setError(message: String) {
        _lastError.value = message
        _state.value = VoiceState.ERROR
    }

    fun speak(text: String) {
        if (!currentVoiceSettings.voiceEnabled || !currentVoiceSettings.autoSpeak) {
            _state.value = VoiceState.IDLE
            return
        }
        scope.launch {
            ttsManager.speak(text, currentVoiceSettings)
        }
    }

    fun stopSpeaking() {
        ttsManager.stopSpeaking()
    }

    suspend fun availableTtsLanguages() = ttsManager.availableLanguagesSupported()

    fun currentVoiceSettingsSnapshot(): VoiceSettings = currentVoiceSettings

    fun shutdown() {
        recognizerManager.destroy()
        ttsManager.shutdown()
    }
}
