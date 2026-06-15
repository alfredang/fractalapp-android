package com.tertiaryinfotech.fractalapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FractalColors = darkColorScheme(
    primary = Color(0xFFC16BFF),
    onPrimary = Color(0xFF1A0A2E),
    secondary = Color(0xFF4ED8C8),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF2EAFF),
    surface = Color(0xFF14101F),
    onSurface = Color(0xFFF2EAFF),
)

@Composable
fun FractalTheme(content: @Composable () -> Unit) {
    // Always dark — fractals look best on black, matching the iOS app.
    @Suppress("UNUSED_EXPRESSION") isSystemInDarkTheme()
    MaterialTheme(colorScheme = FractalColors, content = content)
}
