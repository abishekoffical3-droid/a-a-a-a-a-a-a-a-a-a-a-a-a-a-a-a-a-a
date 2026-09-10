package com.priya.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PriyaColorScheme = darkColorScheme(
    primary = CrimsonCore,
    onPrimary = SoftWhite,
    secondary = EmberOrange,
    onSecondary = SoftWhite,
    background = VoidBlack,
    onBackground = SoftWhite,
    surface = PanelBlack,
    onSurface = SoftWhite,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = MutedWhite,
    error = StatusRed,
    outline = GlassBorder
)

@Composable
fun PriyaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // PRIYA is always dark/crimson by design (spec §4/§29) — darkTheme param kept
    // for API familiarity but intentionally does not switch to a light palette.
    MaterialTheme(
        colorScheme = PriyaColorScheme,
        typography = PriyaTypography,
        content = content
    )
}
