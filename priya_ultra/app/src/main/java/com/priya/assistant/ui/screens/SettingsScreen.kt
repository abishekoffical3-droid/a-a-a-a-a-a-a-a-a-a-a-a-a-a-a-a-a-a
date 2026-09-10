package com.priya.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priya.assistant.ui.components.GlassPanel
import com.priya.assistant.ui.navigation.Routes
import com.priya.assistant.ui.theme.CrimsonBright
import com.priya.assistant.ui.theme.FaintWhite
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.VoidBlack

private data class SettingsEntry(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@Composable
fun SettingsScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val entries = listOf(
        SettingsEntry("AI Provider", "Gemini, OpenAI, or custom endpoint", Icons.Filled.Psychology, Routes.AI_SETTINGS),
        SettingsEntry("Voice", "Speech, language, TTS engine", Icons.Filled.RecordVoiceOver, Routes.VOICE_SETTINGS),
        SettingsEntry("Accessibility", "Device-control permission", Icons.Filled.AccessibilityNew, Routes.ACCESSIBILITY),
        SettingsEntry("Permissions", "Microphone & notifications", Icons.Filled.Security, Routes.PERMISSIONS),
        SettingsEntry("Privacy", "What PRIYA stores and sends", Icons.Filled.Lock, Routes.PRIVACY),
        SettingsEntry("About", "Version & app info", Icons.Filled.Info, Routes.ABOUT)
    )

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(VoidBlack).padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = SoftWhite)
                }
                Text("Settings", color = SoftWhite, fontSize = 20.sp)
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                entries.forEach { entry ->
                    GlassPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onNavigate(entry.route) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(entry.icon, contentDescription = null, tint = CrimsonBright)
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(entry.title, color = SoftWhite, fontSize = 15.sp)
                                    Text(entry.subtitle, color = MutedWhite, fontSize = 12.sp)
                                }
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = FaintWhite)
                        }
                    }
                }
            }
        }
    }
}
