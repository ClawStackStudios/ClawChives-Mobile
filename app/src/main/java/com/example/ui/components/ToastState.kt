package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ToastMessage(
    val id: Long,
    val message: String,
    val isError: Boolean
)

class ToastState(private val coroutineScope: CoroutineScope) {
    private val _messages = mutableStateListOf<ToastMessage>()
    val messages: List<ToastMessage> get() = _messages
    private var idCounter = 0L

    fun show(message: String, isError: Boolean = false) {
        val toast = ToastMessage(idCounter++, message, isError)
        _messages.add(toast)
        coroutineScope.launch {
            delay(3000L)
            _messages.remove(toast)
        }
    }
}

val LocalToastState = compositionLocalOf<ToastState> { error("No ToastState provided") }
