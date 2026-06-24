package com.example.data.repository

import com.example.data.local.AppConfig
import com.example.data.local.AppConfigDao
import com.example.data.remote.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthRepository(
    private val appConfigDao: AppConfigDao
) {

    private val reauthMutex = Mutex()
    private val _sessionRefreshed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionRefreshed: SharedFlow<Unit> = _sessionRefreshed.asSharedFlow()

    val serverUrl: Flow<String?> = appConfigDao.getConfigFlow().map { it?.serverUrl }.distinctUntilChanged()
    val rawKey: Flow<String?> = appConfigDao.getConfigFlow().map { it?.rawKey }.distinctUntilChanged()

    suspend fun loginWithKey(serverUrl: String, key: String): Result<Unit> {
        return try {
            val client = ApiClient.getClient(serverUrl)
            val responseResult = client.authenticate(key)
            
            if (responseResult.isSuccess) {
                val sessionData = responseResult.getOrThrow()
                // Save config
                val currentConfig = appConfigDao.getConfigSync() ?: AppConfig()
                appConfigDao.insertConfig(currentConfig.copy(
                    serverUrl = serverUrl,
                    authToken = sessionData.token,
                    rawKey = key
                ))
                
                // Update API Client
                ApiClient.updateAuthToken(sessionData.token)
                Result.success(Unit)
            } else {
                Result.failure(responseResult.exceptionOrNull() ?: Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun attemptAutoReauth(): Boolean {
        return reauthMutex.withLock {
            val config = appConfigDao.getConfigSync() ?: return false
            val serverUrlVal = config.serverUrl ?: return false
            val rawKeyVal = config.rawKey ?: return false
            
            val result = loginWithKey(serverUrlVal, rawKeyVal)
            if (result.isSuccess) {
                _sessionRefreshed.tryEmit(Unit)
                true
            } else {
                false
            }
        }
    }

    suspend fun loadExistingSession(): Boolean {
        val config = appConfigDao.getConfigSync()
        val serverUrlVal = config?.serverUrl
        val token = config?.authToken
        
        if (!serverUrlVal.isNullOrEmpty() && !token.isNullOrEmpty()) {
            ApiClient.getClient(serverUrlVal)
            ApiClient.updateAuthToken(token)
            return true
        }
        return false
    }

    suspend fun logout() {
        val config = appConfigDao.getConfigSync()
        if (config != null) {
            appConfigDao.insertConfig(config.copy(authToken = null, rawKey = null))
        }
        ApiClient.updateAuthToken(null)
    }
}
