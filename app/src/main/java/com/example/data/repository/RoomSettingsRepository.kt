package com.example.data.repository

import com.example.data.local.AppConfig
import com.example.data.local.AppConfigDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomSettingsRepository(
    private val appConfigDao: AppConfigDao
) : SettingsRepository {

    override val serverUrl: Flow<String?> = appConfigDao.getConfigFlow()
        .map { it?.serverUrl }
        .distinctUntilChanged()

    override val theme: Flow<String> = appConfigDao.getConfigFlow()
        .map { it?.theme ?: "SYSTEM" }
        .distinctUntilChanged()

    override suspend fun saveServerUrl(url: String) {
        withContext(Dispatchers.IO) {
            val config = appConfigDao.getConfigSync() ?: AppConfig()
            appConfigDao.insertConfig(config.copy(serverUrl = url))
        }
    }

    override suspend fun saveTheme(theme: String) {
        withContext(Dispatchers.IO) {
            val config = appConfigDao.getConfigSync() ?: AppConfig()
            appConfigDao.insertConfig(config.copy(theme = theme))
        }
    }

    override suspend fun clearSettings() {
        withContext(Dispatchers.IO) {
            val config = appConfigDao.getConfigSync() ?: AppConfig()
            appConfigDao.insertConfig(config.copy(serverUrl = null, theme = "SYSTEM"))
        }
    }
}
