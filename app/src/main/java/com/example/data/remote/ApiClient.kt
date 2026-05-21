package com.example.data.remote

object ApiClient {
    private var currentClient: ClawChivesClient? = null
    var authToken: String? = null
        private set
    var currentServerUrl: String? = null
        private set
        
    var onUnauthorizedCallback: (() -> Unit)? = null

    fun updateAuthToken(token: String?) {
        authToken = token
    }

    fun getCurrentClient(): ClawChivesClient {
        return currentClient ?: throw IllegalStateException("API Client has not been initialized with a base URL yet.")
    }

    @Synchronized
    fun getClient(baseUrl: String): ClawChivesClient {
        if (currentServerUrl == baseUrl && currentClient != null) {
            return currentClient!!
        }

        currentClient?.close()
        currentServerUrl = baseUrl

        currentClient = ClawChivesClient(
            baseUrl = baseUrl,
            onUnauthorized = {
                onUnauthorizedCallback?.invoke()
            }
        )
        return currentClient!!
    }
}
