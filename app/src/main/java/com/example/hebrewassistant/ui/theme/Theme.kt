package com.example.hebrewassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue40,
    onPrimary = Blue99,
    secondary = Teal40,
    onSecondary = Teal99,
    background = Grey99,
    onBackground = Grey10,
    surface = Grey98,
    onSurface = Grey10
)

private val DarkColors = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    secondary = Teal80,
    onSecondary = Teal20,
    background = Grey10,
    onBackground = Grey90,
    surface = Grey12,
    onSurface = Grey90
)

@Composable
fun HebrewAssistantTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
