package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1D4ED8),       // Vibrant Royal Blue
    onPrimary = Color.White,
    secondary = Color(0xFFD97706),     // Warm Amber
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9),    // Clean Bright Slate 100
    surface = Color.White,
    onSurface = Color(0xFF0F172A),      // Crisp Dark Slate 900
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Explicit Light Theme requested by user
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
