package com.priya.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.priya.assistant.ui.PriyaViewModel
import com.priya.assistant.ui.components.ChatBubbleRow
import com.priya.assistant.ui.components.ThinkingBubble
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.FaintWhite
import com.priya.assistant.ui.theme.GlassBorder
import com.priya.assistant.ui.theme.GlassSurface
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.VoidBlack
import com.priya.assistant.voice.VoiceState

@Composable
fun ChatScreen(viewModel: PriyaViewModel, onBack: () -> Unit) {
    val conversation by viewModel.activeConversation.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val pending by viewModel.pendingConfirmation.collectAsStateWithLifecycle()
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isThinking = voiceState == VoiceState.PROCESSING || voiceState == VoiceState.EXECUTING

    LaunchedEffect(conversation?.messages?.size, isThinking) {
        val count = (conversation?.messages?.size ?: 0) + if (isThinking) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(VoidBlack).padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = SoftWhite)
                    }
                    Text(
                        conversation?.title?.takeIf { it.isNotBlank() } ?: "New Chat",
                        color = SoftWhite,
                        fontSize = 16.sp
                    )
                }
                Row {
                    IconButton(onClick = { viewModel.startNewConversation() }) {
                        Icon(Icons.Filled.Add, contentDescription = "New chat", tint = MutedWhite)
                    }
                    IconButton(onClick = { viewModel.clearAllHistory() }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear all history", tint = MutedWhite)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val messages = conversation?.messages.orEmpty()
                items(messages, key = { it.id }) { message ->
                    ChatBubbleRow(
                        message = message,
                        isSpeaking = voiceState == VoiceState.SPEAKING,
                        onCopy = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(message.text)) },
                        onRegenerate = if (message == messages.lastOrNull { it.sender == com.priya.assistant.data.models.MessageSender.PRIYA }) {
                            { viewModel.sendUserMessage(messages.lastOrNull { m -> m.sender == com.priya.assistant.data.models.MessageSender.USER }?.text ?: "") }
                        } else null,
                        onStopSpeaking = if (voiceState == VoiceState.SPEAKING) viewModel::stopSpeaking else null
                    )
                }
                if (isThinking) {
                    item { ThinkingBubble() }
                }
            }

            ChatInputBar(
                input = input,
                onInputChange = { input = it },
                isListening = voiceState == VoiceState.LISTENING,
                onMicClick = viewModel::onMicPressed,
                onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendUserMessage(input.trim())
                        input = ""
                    }
                }
            )
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
private fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    isListening: Boolean,
    onMicClick: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message PRIYA...", color = FaintWhite) },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SoftWhite,
                unfocusedTextColor = SoftWhite,
                focusedContainerColor = GlassSurface,
                unfocusedContainerColor = GlassSurface,
                focusedBorderColor = CrimsonCore,
                unfocusedBorderColor = GlassBorder
            ),
            maxLines = 4
        )
        Spacer(Modifier.width(6.dp))
        IconButton(
            onClick = onMicClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isListening) CrimsonCore else GlassSurface)
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Voice input", tint = SoftWhite, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(6.dp))
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CrimsonCore)
        ) {
            Icon(Icons.Filled.Send, contentDescription = "Send", tint = SoftWhite, modifier = Modifier.size(18.dp))
        }
    }
}
