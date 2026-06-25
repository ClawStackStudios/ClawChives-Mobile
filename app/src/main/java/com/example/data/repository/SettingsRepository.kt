package com.example.data.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val serverUrl: Flow<String?>
    val theme: Flow<String>
    
    suspend fun saveServerUrl(url: String)
    suspend fun saveTheme(theme: String)
    suspend fun clearSettings()
}
