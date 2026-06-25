package com.example.ui.feature.gateway

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.AuthRepository

import com.example.data.repository.SettingsRepository

class GatewayViewModelFactory(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GatewayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GatewayViewModel(authRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
