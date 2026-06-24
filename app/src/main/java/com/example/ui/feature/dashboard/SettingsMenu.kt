package com.example.ui.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.border
import com.example.ui.theme.AppTheme
import com.example.ui.theme.LocalThemeState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DashedBorder
import com.example.ui.theme.WarningText
import com.example.ui.theme.RedAccent
import com.example.data.remote.ApiClient
import com.example.data.remote.DiagnosticsService
import com.example.data.remote.DiagnosticResult

@Composable
fun SettingsMenu(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "THEME",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ThemeSelectionGroup()

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "ABOUT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                AboutSection()
            }
        }
    }
}

@Composable
fun AboutSection() {
    var diagnostics by remember { mutableStateOf<List<DiagnosticResult>?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isChecking = true
        try {
            val token = ApiClient.authToken
            val client = ApiClient.getCurrentClient()
            if (token != null) {
                val service = DiagnosticsService(client)
                diagnostics = service.runDiagnostics(token)
            }
        } catch (e: Exception) {
            // Error handled below
        } finally {
            isChecking = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.1f))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = "ClawChives Mobile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        Text(text = "Version 1.0.0", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Server Connectivity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (isChecking) {
            Text("Checking parity...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
        } else {
            diagnostics?.let { results ->
                results.forEach { result ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (result.success && result.parityValid) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (result.success && result.parityValid) CyanAccent else RedAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(result.endpoint, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            Text(
                                text = if (result.success && result.parityValid) "Parity OK (${result.dataSize} items)" else result.message,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } ?: Text("Unable to run diagnostics", color = RedAccent, fontSize = 14.sp)
        }
    }
}

@Composable
fun ThemeSelectionGroup() {
    val themeState = LocalThemeState.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.2f))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
    ) {
        ThemeOption(
            title = "System",
            selected = themeState.theme == AppTheme.SYSTEM,
            onClick = { offset -> themeState.setTheme(AppTheme.SYSTEM, offset) },
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
        )
        Box(
            modifier = Modifier
              .width(1.dp)
              .height(24.dp)
              .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
              .align(Alignment.CenterVertically)
        )
        ThemeOption(
            title = "Light",
            selected = themeState.theme == AppTheme.LIGHT,
            onClick = { offset -> themeState.setTheme(AppTheme.LIGHT, offset) },
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
              .width(1.dp)
              .height(24.dp)
              .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
              .align(Alignment.CenterVertically)
        )
        ThemeOption(
            title = "Dark",
            selected = themeState.theme == AppTheme.DARK,
            onClick = { offset -> themeState.setTheme(AppTheme.DARK, offset) },
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
              .width(1.dp)
              .height(24.dp)
              .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
              .align(Alignment.CenterVertically)
        )
        ThemeOption(
            title = "OLED",
            selected = themeState.theme == AppTheme.OLED,
            onClick = { offset -> themeState.setTheme(AppTheme.OLED, offset) },
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var centerOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val size = coordinates.size
                val position = coordinates.positionInRoot()
                centerOffset = Offset(
                    x = position.x + size.width / 2f,
                    y = position.y + size.height / 2f
                )
            }
            .clickable { onClick(centerOffset) }
            .background(if (selected) CyanAccent.copy(alpha = 0.15f) else Color.Transparent)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) CyanAccent else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}
