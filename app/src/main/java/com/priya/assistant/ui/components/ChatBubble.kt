package com.priya.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priya.assistant.data.models.ChatMessage
import com.priya.assistant.data.models.MessageSender
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.CrimsonDeep
import com.priya.assistant.ui.theme.FaintWhite
import com.priya.assistant.ui.theme.GlassBorder
import com.priya.assistant.ui.theme.GlassSurface
import com.priya.assistant.ui.theme.SoftWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubbleRow(
    message: ChatMessage,
    isSpeaking: Boolean,
    onCopy: () -> Unit,
    onRegenerate: (() -> Unit)?,
    onStopSpeaking: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) CrimsonDeep.copy(alpha = 0.55f) else GlassSurface)
                    .border(
                        1.dp,
                        if (isUser) CrimsonCore.copy(alpha = 0.5f) else GlassBorder,
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(message.text, color = SoftWhite, fontSize = 15.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = formatTime(message.timestamp),
                    color = FaintWhite,
                    fontSize = 10.sp
                )
                if (!isUser) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = FaintWhite, modifier = Modifier.size(14.dp))
                    }
                    if (onRegenerate != null) {
                        IconButton(onClick = onRegenerate, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Regenerate", tint = FaintWhite, modifier = Modifier.size(14.dp))
                        }
                    }
                    if (isSpeaking && onStopSpeaking != null) {
                        IconButton(onClick = onStopSpeaking, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop speaking", tint = CrimsonCore, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingBubble(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("PRIYA is thinking...", color = FaintWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
