package com.example.ui.theme

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalView
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
    var revealCenter by remember { mutableStateOf(Offset.Zero) }
    val revealProgress = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    val view = LocalView.current
    var capturedImage by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    val setTheme: (AppTheme, Offset) -> Unit = { newTheme, center ->
        if (newTheme != currentTheme) {
            try {
                // Capture the current ComposeView before changing the theme
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                capturedImage = bitmap.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            currentTheme = newTheme
            revealCenter = center
            scope.launch {
                revealProgress.snapTo(0f)
                revealProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
                capturedImage = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Draw the captured image (old theme) at the bottom
        capturedImage?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Draw the new theme, masked by the circular reveal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (revealProgress.value < 1f) Modifier.clip(CircularRevealShape(revealProgress.value, revealCenter))
                    else Modifier
                )
        ) {
            MyApplicationTheme(theme = currentTheme) {
                content(currentTheme, setTheme)
            }
        }
    }
}
