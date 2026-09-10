package com.priya.assistant.voice

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.priya.assistant.MainActivity
import com.priya.assistant.PriyaApplication

/**
 * Foreground service that exists purely to keep a persistent "PRIYA is
 * listening" notification (with a Stop action) visible while continuous
 * listening is on, per spec §23/§24. It never starts itself — only an
 * explicit user action (enabling Continuous Listening in Voice settings)
 * starts it, and disabling that setting stops it immediately.
 */
class PriyaVoiceService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as PriyaApplication).voiceManager.stopListening()
    }

    private fun buildNotification(): android.app.Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, PriyaVoiceService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, PriyaApplication.VOICE_CHANNEL_ID)
            .setContentTitle("PRIYA is listening")
            .setContentText("Tap Stop to end continuous listening.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openAppIntent)
            .addAction(0, "Stop", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 4201
        const val ACTION_STOP = "com.priya.assistant.voice.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, PriyaVoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PriyaVoiceService::class.java))
        }
    }
}
