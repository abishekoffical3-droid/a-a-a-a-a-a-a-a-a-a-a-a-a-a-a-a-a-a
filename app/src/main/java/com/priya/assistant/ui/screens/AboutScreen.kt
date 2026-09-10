package com.priya.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priya.assistant.ui.components.GlassPanel
import com.priya.assistant.ui.components.PriyaAvatar
import com.priya.assistant.ui.theme.CrimsonBright
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.VoidBlack

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(containerColor = VoidBlack) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(VoidBlack).padding(padding)) {
            ScreenHeader("About", onBack)

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PriyaAvatar(sizeDp = 72.dp)
                Spacer(Modifier.height(10.dp))
                Text("PRIYA", color = SoftWhite, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("Personal AI Companion", color = MutedWhite, fontSize = 13.sp)
                Text("Version 1.0.0", color = CrimsonBright, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(Modifier.height(20.dp))

                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Text("Built with", color = SoftWhite, fontSize = 14.sp)
                    Text("Kotlin + Jetpack Compose + Material 3", color = MutedWhite, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(Modifier.height(12.dp))

                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Text("AI providers", color = SoftWhite, fontSize = 14.sp)
                    Text(
                        "Gemini, OpenAI, or any OpenAI-compatible custom endpoint you configure in Settings \u2192 AI. PRIYA works as a basic device assistant even without one configured.",
                        color = MutedWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Text("Accessibility & privacy", color = SoftWhite, fontSize = 14.sp)
                    Text(
                        "Device-control features use Android's Accessibility service only when you ask PRIYA to act. See Settings \u2192 Privacy for full details.",
                        color = MutedWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
