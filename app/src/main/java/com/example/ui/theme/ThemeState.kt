package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Offset

enum class AppTheme {
    SYSTEM, LIGHT, DARK, OLED
}

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var theme: AppTheme
        get() {
            val name = prefs.getString("current_theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
            return try { AppTheme.valueOf(name) } catch (e: Exception) { AppTheme.SYSTEM }
        }
        set(value) {
            prefs.edit().putString("current_theme", value.name).apply()
        }
}

data class ThemeState(
    val theme: AppTheme,
    val setTheme: (AppTheme, Offset) -> Unit
)

val LocalThemeState = compositionLocalOf<ThemeState> { error("No ThemeState provided") }
