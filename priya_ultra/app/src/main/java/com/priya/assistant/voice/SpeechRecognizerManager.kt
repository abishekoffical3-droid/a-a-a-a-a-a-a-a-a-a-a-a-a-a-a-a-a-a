package com.priya.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.priya.assistant.data.models.SpeechLanguage

/**
 * Wraps Android's SpeechRecognizer with the restart/backoff behavior required by
 * spec §6: recognition must not permanently die after one utterance, but must
 * also never spin in a tight restart loop (e.g. when the mic is denied).
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val onPartialState: (VoiceState) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var continuousModeEnabled = false
    private var currentLanguage: SpeechLanguage = SpeechLanguage.AUTO
    private var consecutiveErrorCount = 0
    private var manuallyStopped = false

    private val restartRunnable = Runnable { startListeningInternal() }

    fun setContinuousMode(enabled: Boolean) {
        continuousModeEnabled = enabled
    }

    fun setLanguage(language: SpeechLanguage) {
        currentLanguage = language
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        manuallyStopped = false
        mainHandler.removeCallbacks(restartRunnable)
        startListeningInternal()
    }

    fun stopListening() {
        manuallyStopped = true
        mainHandler.removeCallbacks(restartRunnable)
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    fun destroy() {
        manuallyStopped = true
        mainHandler.removeCallbacks(restartRunnable)
        recognizer?.destroy()
        recognizer = null
    }

    private fun startListeningInternal() {
        if (manuallyStopped) return
        if (!isAvailable()) {
            onError("Speech recognition isn't available on this device.")
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }

        val languageTag = when (currentLanguage) {
            SpeechLanguage.AUTO -> java.util.Locale.getDefault().toLanguageTag()
            else -> currentLanguage.tag
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            // Note: stock Android SpeechRecognizer only accepts a single recognition
            // language per session. True per-utterance language auto-detection across
            // Nepali/Hindi/English isn't exposed by the public API, so AUTO mode uses
            // the device's default locale — the honest limit of what this API allows.
        }

        onPartialState(VoiceState.LISTENING)
        recognizer?.startListening(intent)
    }

    private fun scheduleRestart(delayMs: Long) {
        if (manuallyStopped || !continuousModeEnabled) return
        mainHandler.removeCallbacks(restartRunnable)
        mainHandler.postDelayed(restartRunnable, delayMs)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            consecutiveErrorCount = 0
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            onPartialState(VoiceState.PROCESSING)
        }

        override fun onError(error: Int) {
            consecutiveErrorCount++
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    // Normal "heard nothing" case — just go quiet and restart if continuous.
                    onPartialState(VoiceState.IDLE)
                    scheduleRestart(backoffDelay())
                }
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    onError("Microphone permission is required.")
                }
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                    onError("I can't reach the speech service right now.")
                    scheduleRestart(backoffDelay())
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                    scheduleRestart(400L)
                }
                else -> {
                    onPartialState(VoiceState.IDLE)
                    scheduleRestart(backoffDelay())
                }
            }
        }

        override fun onResults(results: Bundle?) {
            consecutiveErrorCount = 0
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotBlank()) {
                onResult(text)
            } else {
                onPartialState(VoiceState.IDLE)
                scheduleRestart(300L)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /** Exponential-ish backoff capped at 4s to avoid a tight restart loop on persistent errors. */
    private fun backoffDelay(): Long = (300L * (consecutiveErrorCount + 1)).coerceAtMost(4000L)
}
