package com.example.data.repository

import com.example.data.local.AuthPreferences
import com.example.data.remote.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthRepository(private val authPreferences: AuthPreferences) {

    private val reauthMutex = Mutex()
    private val _sessionRefreshed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionRefreshed: SharedFlow<Unit> = _sessionRefreshed.asSharedFlow()

    val serverUrl: Flow<String?> = authPreferences.serverUrl

    suspend fun loginWithKey(serverUrl: String, key: String): Result<Unit> {
        return try {
            val client = ApiClient.getClient(serverUrl)
            val responseResult = client.authenticate(key)
            
            if (responseResult.isSuccess) {
                val sessionData = responseResult.getOrThrow()
                // Save preferences
                authPreferences.saveServerUrl(serverUrl)
                authPreferences.saveAuthToken(sessionData.token)
                authPreferences.saveRawKey(key)
                
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
            val serverUrlVal = authPreferences.serverUrl.first() ?: return false
            val rawKeyVal = authPreferences.getRawKeySync() ?: return false
            
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
        val serverUrlVal = authPreferences.serverUrl.first()
        val token = authPreferences.authToken.first()
        
        if (!serverUrlVal.isNullOrEmpty() && !token.isNullOrEmpty()) {
            ApiClient.getClient(serverUrlVal)
            ApiClient.updateAuthToken(token)
            return true
        }
        return false
    }

    suspend fun logout() {
        authPreferences.clearSession()
        ApiClient.updateAuthToken(null)
    }
}
