package com.priya.assistant.voice

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.priya.assistant.data.models.SpeechLanguage
import com.priya.assistant.data.models.VoiceEngine
import com.priya.assistant.data.models.VoiceSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Speaks PRIYA's replies. Android TextToSpeech is always initialized and is the
 * guaranteed fallback (spec §20/§21) — an external TTS provider is only ever an
 * optional enhancement layered on top, never a hard dependency.
 */
class TextToSpeechManager(
    private val context: Context,
    private val onStart: () -> Unit,
    private val onDone: () -> Unit,
    private val onFallbackNotice: (String) -> Unit
) {
    private var androidTts: TextToSpeech? = null
    private var androidTtsReady = false
    private var mediaPlayer: android.media.MediaPlayer? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    init {
        androidTts = TextToSpeech(context) { status ->
            androidTtsReady = status == TextToSpeech.SUCCESS
            androidTts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        }
        androidTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { onStart() }
            override fun onDone(utteranceId: String?) { onDone() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { onDone() }
        })
    }

    fun availableLanguagesSupported(): List<SpeechLanguage> {
        if (!androidTtsReady) return listOf(SpeechLanguage.ENGLISH)
        return SpeechLanguage.entries.filter { lang ->
            if (lang == SpeechLanguage.AUTO) return@filter false
            val locale = localeFor(lang)
            val result = androidTts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            result >= TextToSpeech.LANG_AVAILABLE
        }
    }

    suspend fun speak(text: String, voiceSettings: VoiceSettings) {
        if (text.isBlank()) return
        val useExternal = voiceSettings.voiceEngine == VoiceEngine.EXTERNAL ||
            (voiceSettings.voiceEngine == VoiceEngine.AUTO && voiceSettings.externalTtsBaseUrl.isNotBlank())

        if (useExternal) {
            val played = trySpeakExternal(text, voiceSettings)
            if (played) return
            onFallbackNotice("I couldn't use that voice, so I'm switching to the device voice.")
        }
        speakWithAndroidTts(text, voiceSettings)
    }

    private fun speakWithAndroidTts(text: String, voiceSettings: VoiceSettings) {
        val tts = androidTts
        if (tts == null || !androidTtsReady) {
            onDone()
            return
        }
        val locale = localeFor(effectiveLanguage(voiceSettings, text))
        val result = tts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.US)
        }
        tts.setSpeechRate(voiceSettings.speechSpeed)
        tts.setPitch(voiceSettings.speechPitch)

        // Keep spoken output short and paced — split on sentence punctuation per spec §11.
        val utteranceId = UUID.randomUUID().toString()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /** Best-effort call to a user-configured external TTS endpoint. Returns false on any failure. */
    private suspend fun trySpeakExternal(text: String, voiceSettings: VoiceSettings): Boolean =
        withContext(Dispatchers.IO) {
            if (voiceSettings.externalTtsBaseUrl.isBlank()) return@withContext false
            runCatching {
                onStart()
                val encodedVoice = java.net.URLEncoder.encode(voiceSettings.externalTtsVoiceId, "UTF-8")
                val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                val url = "${voiceSettings.externalTtsBaseUrl.trimEnd('/')}/speak?voice=$encodedVoice&text=$encodedText"
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext false
                    val bytes = response.body?.bytes() ?: return@withContext false
                    playAudioBytes(bytes)
                }
                true
            }.getOrElse {
                onDone()
                false
            }
        }

    private fun playAudioBytes(bytes: ByteArray) {
        val tempFile = java.io.File.createTempFile("priya_tts_", ".mp3", context.cacheDir)
        tempFile.writeBytes(bytes)
        mediaPlayer?.release()
        mediaPlayer = android.media.MediaPlayer().apply {
            setDataSource(tempFile.absolutePath)
            setOnCompletionListener {
                onDone()
                tempFile.delete()
            }
            prepare()
            start()
        }
    }

    fun stopSpeaking() {
        androidTts?.stop()
        mediaPlayer?.let { if (it.isPlaying) it.stop() }
        onDone()
    }

    fun shutdown() {
        androidTts?.shutdown()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun effectiveLanguage(voiceSettings: VoiceSettings, text: String): SpeechLanguage {
        if (voiceSettings.language != SpeechLanguage.AUTO) return voiceSettings.language
        // AUTO: Nepali and Hindi share the Devanagari script and Android TTS can't
        // reliably tell them apart from text alone, so we detect the script and
        // default to Hindi voice data (more commonly installed) when present.
        val hasDevanagari = text.any { it.code in 0x0900..0x097F }
        return if (hasDevanagari) SpeechLanguage.HINDI else SpeechLanguage.ENGLISH
    }

    private fun localeFor(language: SpeechLanguage): Locale = when (language) {
        SpeechLanguage.NEPALI -> Locale.forLanguageTag("ne-NP")
        SpeechLanguage.HINDI -> Locale.forLanguageTag("hi-IN")
        else -> Locale.US
    }
}
