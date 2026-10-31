package com.omnitool.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LaserCyan,
    onPrimary = IndustrialDark,
    secondary = NeonEmerald,
    onSecondary = IndustrialDark,
    tertiary = CautionAmber,
    onTertiary = IndustrialDark,
    background = IndustrialDark,
    onBackground = TextPrimary,
    surface = IndustrialSurface,
    onSurface = TextPrimary,
    surfaceVariant = IndustrialCard,
    onSurfaceVariant = TextSecondary,
    outline = IndustrialBorder,
    error = DangerCrimson
)

@Composable
fun OmniToolTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
