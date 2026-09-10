package com.priya.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priya.assistant.ui.theme.CrimsonBright
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.CrimsonDeep
import com.priya.assistant.ui.theme.GlassBorder
import com.priya.assistant.ui.theme.GlassSurface
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite

/** Dark glass panel used for cards throughout the app (spec §4 "dark glass panels"). */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        content = content
    )
}

/** Circular PRIYA avatar with a thin animated-feeling red glow ring (spec §31). */
@Composable
fun PriyaAvatar(modifier: Modifier = Modifier, sizeDp: androidx.compose.ui.unit.Dp = 72.dp) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .background(
                Brush.radialGradient(listOf(CrimsonCore.copy(alpha = 0.5f), Color.Transparent)),
                CircleShape
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(CrimsonDeep, CrimsonCore)))
            .border(1.5.dp, CrimsonBright, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("P", color = SoftWhite, fontWeight = FontWeight.Black, fontSize = (sizeDp.value * 0.4).sp)
    }
}

@Composable
fun QuickActionCard(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = CrimsonBright, modifier = Modifier.size(26.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
        Text(
            label,
            color = MutedWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = MutedWhite,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        modifier = modifier.fillMaxWidth().padding(bottom = 6.dp, top = 4.dp)
    )
}
