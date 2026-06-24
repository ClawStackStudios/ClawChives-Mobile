package com.example.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.hypot
import kotlinx.coroutines.launch

class CircularRevealShape(private val progress: Float, private val center: Offset) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radius = progress * hypot(
            maxOf(center.x, size.width - center.x),
            maxOf(center.y, size.height - center.y)
        )
        return Outline.Generic(Path().apply {
            addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
        })
    }
}

@Composable
fun ThemeCircularRevealProvider(
    initialTheme: AppTheme,
    content: @Composable (theme: AppTheme, setTheme: (AppTheme, Offset) -> Unit) -> Unit
) {
    var currentTheme by remember { mutableStateOf(initialTheme) }
    var previousTheme by remember { mutableStateOf(initialTheme) }
    var revealCenter by remember { mutableStateOf(Offset.Zero) }
    val revealProgress = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val setTheme: (AppTheme, Offset) -> Unit = { newTheme, center ->
        if (newTheme != currentTheme) {
            previousTheme = currentTheme
            currentTheme = newTheme
            revealCenter = center
            scope.launch {
                revealProgress.snapTo(0f)
                revealProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600)
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw the previous theme in the background
        if (revealProgress.value < 1f) {
            MyApplicationTheme(theme = previousTheme) {
                content(previousTheme, setTheme)
            }
        }

        // Draw the new theme in the foreground, masked by the circular reveal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircularRevealShape(revealProgress.value, revealCenter))
        ) {
            MyApplicationTheme(theme = currentTheme) {
                content(currentTheme, setTheme)
            }
        }
    }
}
