package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CodexraPrimaryCyan,
    onPrimary = Color(0xFF001F28),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBEE9FF),
    secondary = CodexraAmberAccent,
    onSecondary = Color(0xFF422B00),
    secondaryContainer = Color(0xFF5E3F00),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = CodexraCrimsonAlert,
    onTertiary = Color.White,
    background = CodexraDarkBackground,
    onBackground = CodexraTextPrimary,
    surface = CodexraDarkSurface,
    onSurface = CodexraTextPrimary,
    surfaceVariant = CodexraDarkSurfaceVariant,
    onSurfaceVariant = CodexraTextSecondary,
    outline = Color(0xFF334155),
    error = CodexraCrimsonAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CodexraLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = CodexraLightAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = Color(0xFFE11D48),
    onTertiary = Color.White,
    background = CodexraLightBackground,
    onBackground = CodexraLightTextPrimary,
    surface = CodexraLightSurface,
    onSurface = CodexraLightTextPrimary,
    surfaceVariant = CodexraLightSurfaceVariant,
    onSurfaceVariant = CodexraLightTextSecondary,
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
