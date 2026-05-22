package com.example.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthPreferences(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_clawchives_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val SERVER_URL = "server_url"
    private val AUTH_TOKEN = "auth_token"
    private val RAW_KEY = "raw_key"

    private val _serverUrl = MutableStateFlow(sharedPreferences.getString(SERVER_URL, null))
    val serverUrl: Flow<String?> = _serverUrl.asStateFlow()

    private val _authToken = MutableStateFlow(sharedPreferences.getString(AUTH_TOKEN, null))
    val authToken: Flow<String?> = _authToken.asStateFlow()

    private val _rawKey = MutableStateFlow(sharedPreferences.getString(RAW_KEY, null))
    val rawKey: Flow<String?> = _rawKey.asStateFlow()

    suspend fun saveServerUrl(url: String) {
        sharedPreferences.edit().putString(SERVER_URL, url).apply()
        _serverUrl.value = url
    }

    suspend fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(AUTH_TOKEN, token).apply()
        _authToken.value = token
    }

    suspend fun saveRawKey(key: String) {
        sharedPreferences.edit().putString(RAW_KEY, key).apply()
        _rawKey.value = key
    }
    
    suspend fun clearSession() {
        sharedPreferences.edit().remove(AUTH_TOKEN).remove(RAW_KEY).apply()
        _authToken.value = null
        _rawKey.value = null
    }

    // Helper synchronous getters for immediate access
    fun getServerUrlSync(): String? = sharedPreferences.getString(SERVER_URL, null)
    fun getAuthTokenSync(): String? = sharedPreferences.getString(AUTH_TOKEN, null)
    fun getRawKeySync(): String? = sharedPreferences.getString(RAW_KEY, null)
}
