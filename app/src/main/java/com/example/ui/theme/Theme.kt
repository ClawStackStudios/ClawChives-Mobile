package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = RedAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = DashedBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyanAccent,
    secondary = RedAccent,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkBackground,
    onSurface = DarkBackground,
    surfaceVariant = Color(0xFFE2E8F0)
)

private val OledColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = RedAccent,
    background = Color.Black,
    surface = Color(0xFF0A0A0A), // Very dark gray for surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = Color(0xFF1A1A1A)
)

@Composable
fun MyApplicationTheme(
  theme: AppTheme = AppTheme.DARK,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (theme) {
      AppTheme.SYSTEM -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
      AppTheme.LIGHT -> LightColorScheme
      AppTheme.DARK -> DarkColorScheme
      AppTheme.OLED -> OledColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
