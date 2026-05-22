package com.example.ui.feature.gateway

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface GatewayUiState {
    object Idle : GatewayUiState
    object Loading : GatewayUiState
    data class Success(val hasSession: Boolean) : GatewayUiState
    data class Error(val message: String) : GatewayUiState
}

class GatewayViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val savedServerUrl = authRepository.serverUrl

    private val _uiState = MutableStateFlow<GatewayUiState>(GatewayUiState.Idle)
    val uiState: StateFlow<GatewayUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            _uiState.update { GatewayUiState.Loading }
            val hasSession = authRepository.loadExistingSession()
            if (hasSession) {
                _uiState.update { GatewayUiState.Success(true) }
            } else {
                _uiState.update { GatewayUiState.Idle }
            }
        }
    }

    fun login(serverUrl: String, key: String) {
        if (serverUrl.isBlank() || key.isBlank()) {
            _uiState.update { GatewayUiState.Error("Server URL and Key cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { GatewayUiState.Loading }
            val result = authRepository.loginWithKey(serverUrl, key)
            result.fold(
                onSuccess = {
                    _uiState.update { GatewayUiState.Success(true) }
                },
                onFailure = { error ->
                    _uiState.update { GatewayUiState.Error(error.message ?: "Authentication failed") }
                }
            )
        }
    }
}
