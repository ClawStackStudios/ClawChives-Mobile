package com.example.data.repository

import com.example.data.local.AuthPreferences
import com.example.data.remote.ApiClient
import kotlinx.coroutines.flow.first

class AuthRepository(private val authPreferences: AuthPreferences) {

    suspend fun loginWithKey(serverUrl: String, key: String): Result<Unit> {
        return try {
            val client = ApiClient.getClient(serverUrl)
            val responseResult = client.authenticate(key)
            
            if (responseResult.isSuccess) {
                val sessionData = responseResult.getOrThrow()
                // Save preferences
                authPreferences.saveServerUrl(serverUrl)
                authPreferences.saveAuthToken(sessionData.token)
                
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

    suspend fun loadExistingSession(): Boolean {
        val serverUrl = authPreferences.serverUrl.first()
        val token = authPreferences.authToken.first()
        
        if (!serverUrl.isNullOrEmpty() && !token.isNullOrEmpty()) {
            ApiClient.getClient(serverUrl)
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
