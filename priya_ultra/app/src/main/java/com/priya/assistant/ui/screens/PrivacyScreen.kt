package com.priya.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priya.assistant.ui.components.GlassPanel
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.VoidBlack

private data class PrivacyPoint(val title: String, val body: String)

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val points = listOf(
        PrivacyPoint("API keys", "Stored only on this device, encrypted with the Android Keystore. They are never logged, backed up, or sent anywhere except directly to the AI provider you configured, over HTTPS."),
        PrivacyPoint("Conversations", "Kept locally on your device. A short, trimmed slice of your recent conversation is sent to your selected AI provider only when you send a message, so it can generate a reply — never to any other service."),
        PrivacyPoint("Accessibility", "Used only to carry out device-control actions you explicitly asked PRIYA for — opening apps, tapping, typing, scrolling, going back or home. It does not run in the background collecting screen content."),
        PrivacyPoint("Microphone", "Used only to capture what you say while listening is active, so it can be turned into text. Audio is not stored or uploaded as raw audio unless your chosen AI/voice provider's request itself requires it (e.g. an external TTS you configure)."),
        PrivacyPoint("Continuous listening", "Off by default. You turn it on in Voice settings, and a persistent notification shows whenever it's active, with a Stop action.")
    )

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(VoidBlack).padding(padding).verticalScroll(rememberScrollState())
        ) {
            ScreenHeader("Privacy", onBack)
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                points.forEach { point ->
                    GlassPanel(modifier = Modifier.fillMaxWidth()) {
                        Text(point.title, color = SoftWhite, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(point.body, color = MutedWhite, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }
        }
    }
}
