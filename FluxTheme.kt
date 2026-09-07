package com.azteci.fluxdroid.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FluxColors = darkColorScheme(
    primary = Color(0xFFB8FF4A),
    onPrimary = Color(0xFF0B0D12),
    secondary = Color(0xFF7DE7FF),
    tertiary = Color(0xFFFF7A9A),
    background = Color(0xFF080A0F),
    surface = Color(0xFF12151D),
    surfaceVariant = Color(0xFF1A1E28),
)

@Composable
fun FluxDroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FluxColors, content = content)
}
