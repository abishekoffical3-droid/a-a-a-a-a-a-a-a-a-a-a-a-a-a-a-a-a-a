package com.priya.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.priya.assistant.ai.AIManager
import com.priya.assistant.data.ConversationRepository
import com.priya.assistant.data.SettingsRepository
import com.priya.assistant.security.SecureKeyManager
import com.priya.assistant.voice.VoiceManager

/**
 * Application-level singleton container. Deliberately simple manual DI
 * (no Hilt/Dagger) to keep the build minimal and dependency-free, per spec.
 */
class PriyaApplication : Application() {

    lateinit var secureKeyManager: SecureKeyManager
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var conversationRepository: ConversationRepository
        private set
    lateinit var aiManager: AIManager
        private set
    lateinit var voiceManager: VoiceManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        secureKeyManager = SecureKeyManager(this)
        settingsRepository = SettingsRepository(this)
        conversationRepository = ConversationRepository(this)
        aiManager = AIManager(secureKeyManager, settingsRepository)
        voiceManager = VoiceManager(this, settingsRepository)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VOICE_CHANNEL_ID,
                "PRIYA Voice",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when PRIYA is actively listening"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val VOICE_CHANNEL_ID = "priya_voice_channel"
        lateinit var instance: PriyaApplication
            private set
    }
}
