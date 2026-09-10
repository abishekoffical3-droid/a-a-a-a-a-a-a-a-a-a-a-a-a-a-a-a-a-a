package com.priya.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.priya.assistant.ui.theme.CrimsonBright
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.CrimsonDeep
import com.priya.assistant.ui.theme.CrimsonGlow
import com.priya.assistant.ui.theme.EmberOrange
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.StatusRed
import com.priya.assistant.voice.VoiceState
import kotlin.math.cos
import kotlin.math.sin

/**
 * The futuristic AI-core visual described in spec §4/§30. One composable, six
 * distinct animation characters driven purely by [state] — idle breathing,
 * faster listening pulse, accelerating thinking orbit, speech-reactive core,
 * busy executing orbit, and a brief error flash.
 */
@Composable
fun PriyaCore(
    state: VoiceState,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 220.dp
) {
    val infinite = rememberInfiniteTransition(label = "priya_core")

    val orbitSpeedMs = when (state) {
        VoiceState.IDLE -> 9000
        VoiceState.LISTENING -> 5000
        VoiceState.PROCESSING -> 2200
        VoiceState.EXECUTING -> 1800
        VoiceState.SPEAKING -> 4000
        VoiceState.PAUSED -> 12000
        VoiceState.ERROR -> 800
    }

    val orbitRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(orbitSpeedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation"
    )

    val pulseSpeedMs = when (state) {
        VoiceState.IDLE -> 2600
        VoiceState.LISTENING -> 900
        VoiceState.PROCESSING -> 700
        VoiceState.EXECUTING -> 650
        VoiceState.SPEAKING -> 420
        VoiceState.PAUSED -> 3200
        VoiceState.ERROR -> 220
    }

    val pulseMin = if (state == VoiceState.ERROR) 0.90f else 0.86f
    val pulse by infinite.animateFloat(
        initialValue = pulseMin,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val coreColor = if (state == VoiceState.ERROR) StatusRed else CrimsonCore
    val ringCount = 3

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f

            // Outer glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CrimsonGlow, Color.Transparent),
                    center = center,
                    radius = maxRadius * 1.05f
                ),
                radius = maxRadius * 1.05f,
                center = center
            )

            // Orbital rings
            rotate(degrees = orbitRotation, pivot = center) {
                for (i in 0 until ringCount) {
                    val ringRadius = maxRadius * (0.55f + i * 0.16f)
                    drawCircle(
                        color = CrimsonBright.copy(alpha = 0.22f + i * 0.06f),
                        radius = ringRadius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = (1.4f + i * 0.4f))
                    )
                    // A bright particle riding each ring
                    val angle = Math.toRadians((i * 120).toDouble())
                    val px = center.x + ringRadius * cos(angle).toFloat()
                    val py = center.y + ringRadius * sin(angle).toFloat()
                    drawCircle(
                        color = EmberOrange,
                        radius = 3.5f + i,
                        center = Offset(px, py)
                    )
                }
            }

            // Breathing/pulsing core
            val coreRadius = maxRadius * 0.34f * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SoftWhite.copy(alpha = 0.9f), coreColor, CrimsonDeep),
                    center = center,
                    radius = coreRadius * 1.4f
                ),
                radius = coreRadius,
                center = center
            )
            drawCircle(
                color = coreColor.copy(alpha = 0.35f),
                radius = coreRadius * 1.5f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}

/** Small reusable status dot (for accessibility/AI-provider indicators on Home). */
@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(8.dp)) {
        drawCircle(color = color)
    }
}
