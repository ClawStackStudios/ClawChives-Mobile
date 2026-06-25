package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Offset

enum class AppTheme {
    SYSTEM, LIGHT, DARK, OLED
}

data class ThemeState(
    val theme: AppTheme,
    val setTheme: (AppTheme, Offset) -> Unit
)

val LocalThemeState = compositionLocalOf<ThemeState> { error("No ThemeState provided") }
