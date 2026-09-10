package com.priya.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.priya.assistant.actions.AppLauncher
import com.priya.assistant.ui.PriyaViewModel
import com.priya.assistant.ui.components.PriyaAvatar
import com.priya.assistant.ui.components.PriyaCore
import com.priya.assistant.ui.components.QuickActionCard
import com.priya.assistant.ui.components.SectionLabel
import com.priya.assistant.ui.components.StatusDot
import com.priya.assistant.ui.theme.CrimsonBright
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.FaintWhite
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.StatusAmber
import com.priya.assistant.ui.theme.StatusGreen
import com.priya.assistant.ui.theme.VoidBlack
import com.priya.assistant.voice.VoiceState
import com.priya.assistant.voice.statusLabel

private data class QuickAction(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val run: (AppLauncher) -> Unit)

@Composable
fun HomeScreen(
    viewModel: PriyaViewModel,
    onOpenChat: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appLauncher = remember(context) { AppLauncher(context) }
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val voiceError by viewModel.voiceError.collectAsStateWithLifecycle()
    val activeConversation by viewModel.activeConversation.collectAsStateWithLifecycle()
    val pending by viewModel.pendingConfirmation.collectAsStateWithLifecycle()

    val quickActions = remember {
        listOf(
            QuickAction("YouTube", Icons.Filled.PlayArrow) { it.launchByFriendlyName("youtube") },
            QuickAction("Google", Icons.Filled.Public) { it.webSearch("") },
            QuickAction("Chrome", Icons.Filled.Public) { it.launchByFriendlyName("chrome") },
            QuickAction("Gmail", Icons.Filled.Mail) { it.launchByFriendlyName("gmail") },
            QuickAction("Maps", Icons.Filled.Map) { it.launchByFriendlyName("maps") },
            QuickAction("Instagram", Icons.Filled.Public) { it.launchByFriendlyName("instagram") },
            QuickAction("Facebook", Icons.Filled.Facebook) { it.launchByFriendlyName("facebook") },
            QuickAction("TikTok", Icons.Filled.MusicNote) { it.launchByFriendlyName("tiktok") },
            QuickAction("GitHub", Icons.Filled.Code) { it.launchByFriendlyName("github") },
            QuickAction("Search Web", Icons.Filled.Search) { it.webSearch("") },
            QuickAction("Search YouTube", Icons.Filled.Search) { it.youtubeSearch("") },
            QuickAction("Play Music", Icons.Filled.MusicNote) { it.launchByFriendlyName("spotify") }
        )
    }

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
                .padding(padding)
        ) {
            TopBar(
                accessibilityEnabled = viewModel.isAccessibilityEnabled(),
                aiConfigured = viewModel.isAiConfigured(),
                onOpenSettings = onOpenSettings
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PriyaCore(state = voiceState, size = 210.dp)
                }
                Spacer(Modifier.height(4.dp))
                PriyaAvatar(sizeDp = 56.dp)
                Spacer(Modifier.height(10.dp))
                Text("PRIYA", color = SoftWhite, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("Personal AI Companion", color = MutedWhite, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = voiceError ?: voiceState.statusLabel(),
                    color = if (voiceError != null) StatusAmber else CrimsonBright,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(14.dp))

                MicButton(voiceState = voiceState, onClick = viewModel::onMicPressed)

                Spacer(Modifier.height(16.dp))

                activeConversation?.messages?.lastOrNull()?.let { last ->
                    Text(
                        text = "\u201c${last.text.take(80)}${if (last.text.length > 80) "\u2026" else ""}\u201d",
                        color = FaintWhite,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionLabel("Quick Actions")
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickActions) { action ->
                    QuickActionCard(label = action.label, icon = action.icon) { action.run(appLauncher) }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Open full chat \u2192",
                    color = CrimsonBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onOpenChat)
                )
            }
        }
    }

    pending?.let { p ->
        ConfirmationDialog(
            description = p.description,
            onConfirm = viewModel::confirmPendingAction,
            onCancel = viewModel::cancelPendingAction
        )
    }
}

@Composable
private fun TopBar(accessibilityEnabled: Boolean, aiConfigured: Boolean, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = if (accessibilityEnabled) StatusGreen else StatusAmber)
            Spacer(Modifier.width(6.dp))
            Text(
                if (accessibilityEnabled) "Accessibility on" else "Accessibility off",
                color = MutedWhite, fontSize = 11.sp
            )
            Spacer(Modifier.width(14.dp))
            StatusDot(color = if (aiConfigured) StatusGreen else StatusAmber)
            Spacer(Modifier.width(6.dp))
            Text(if (aiConfigured) "AI ready" else "AI not configured", color = MutedWhite, fontSize = 11.sp)
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MutedWhite)
        }
    }
}

@Composable
private fun MicButton(voiceState: VoiceState, onClick: () -> Unit) {
    val bg = if (voiceState == VoiceState.LISTENING) CrimsonCore else Color(0xFF1A1213)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Mic, contentDescription = "Talk to PRIYA", tint = SoftWhite, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun ConfirmationDialog(description: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Confirm action", color = SoftWhite) },
        text = { Text(description, color = MutedWhite) },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text("CONFIRM", color = CrimsonBright, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onCancel) {
                Text("CANCEL", color = MutedWhite)
            }
        },
        containerColor = Color(0xFF120D0E)
    )
}


