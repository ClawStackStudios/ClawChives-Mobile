package com.example.data.remote

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

suspend fun handleNetworkDiagnostics(response: HttpResponse) {
    if (response.status.value == 400) {
        val responseBody = response.bodyAsText()
        try {
            val errorDetails = Json { ignoreUnknownKeys = true }.decodeFromString<ValidationExceptionResponse>(responseBody)
            Log.e("ClawChivesClient", "🚨 [VALIDATION ERROR 400]: ${errorDetails.error}")
            errorDetails.details.forEach { issue ->
                Log.e("ClawChivesClient", "  → Field [${issue.path}]: ${issue.message}")
            }
        } catch (e: Exception) {
            Log.e("ClawChivesClient", "🚨 [HTTP 400 RAW]: $responseBody", e)
        }
    }
}

class ClawChivesClient(
    private val baseUrl: String, // Format: http://192.168.1.150:4646 or https://domain.com
    private val onUnauthorized: (() -> Unit)? = null
) {
    // 🛡️ Security Check: Ensure standard JSON parser ignores unknown elements (stability lock)
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        defaultRequest {
            url {
                val base = baseUrl.removeSuffix("/")
                takeFrom(base + "/") // ensure trailing slash is handled correctly, Ktor can be picky
            }
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
                    onUnauthorized?.invoke()
                }
            }
        }
    }

    /**
     * 1. Public Health Check
     */
    suspend fun getHealth(): Result<HealthResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/health")
            if (response.status == HttpStatusCode.OK) {
                response.body<HealthResponse>()
            } else {
                throw Exception("Health check failed with status: ${response.status}")
            }
        }
    }

    /**
     * 2. Authentication Handshake
     * Takes the plaintext hu- key, hashes it locally, and requests a session token.
     */
    suspend fun authenticate(rawHumanKey: String): Result<SessionData> = withContext(Dispatchers.IO) {
        runCatching {
            val keyHash = ClawCrypto.hashHumanKey(rawHumanKey)
            val requestBody = TokenRequest(type = if (rawHumanKey.startsWith("hu-")) "human" else "agent", keyHash = keyHash)

            val response: HttpResponse = client.post("api/auth/token") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                val tokenResponse = response.body<TokenResponse>()
                if (tokenResponse.success && tokenResponse.data != null) {
                    tokenResponse.data
                } else {
                    throw Exception(tokenResponse.error ?: "Authentication failed: Unknown error")
                }
            } else {
                throw Exception("Auth request failed: ${response.status.value}")
            }
        }
    }

    /**
     * 3. Fetch Bookmarks
     * Uses the returned api- token in the header.
     */
    suspend fun fetchBookmarks(
        sessionToken: String,
        starred: Boolean? = null,
        archived: Boolean? = null,
        folderId: String? = null,
        search: String? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<List<Bookmark>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/bookmarks") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
                parameter("limit", limit)
                parameter("page", page)
                starred?.let { parameter("starred", it) }
                archived?.let { parameter("archived", it) }
                folderId?.let { parameter("folderId", it) }
                search?.let { parameter("search", it) }
            }

            if (response.status == HttpStatusCode.OK) {
                val listResponse = response.body<BookmarksResponse>()
                listResponse.data
            } else {
                throw Exception("Fetch bookmarks failed: ${response.status.value}")
            }
        }
    }

    /**
     * 4. Create Bookmark
     */
    suspend fun createBookmark(
        sessionToken: String,
        bookmarkRequest: BookmarkCreateRequest
    ): Result<Bookmark> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.post("api/bookmarks") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
                contentType(ContentType.Application.Json)
                setBody(bookmarkRequest.sanitize())
            }
            handleNetworkDiagnostics(response)

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                // Return parsed bookmark response
                val res = response.body<BookmarkResponse>()
                if (res.success) {
                    res.data
                } else {
                    throw Exception("Create bookmark failed on server")
                }
            } else {
                throw Exception("Create bookmark failed: ${response.status.value}")
            }
        }
    }

    /**
     * 5. Update Bookmark
     */
    suspend fun updateBookmark(
        sessionToken: String,
        id: String,
        bookmarkRequest: BookmarkUpdateRequest
    ): Result<Bookmark> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.put("api/bookmarks/$id") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
                contentType(ContentType.Application.Json)
                setBody(bookmarkRequest.sanitize())
            }
            handleNetworkDiagnostics(response)

            if (response.status == HttpStatusCode.OK) {
                val res = response.body<BookmarkResponse>()
                if (res.success) {
                    res.data
                } else {
                    throw Exception("Update bookmark failed on server")
                }
            } else {
                throw Exception("Update bookmark failed: ${response.status.value}")
            }
        }
    }

    /**
     * 6. Fetch Folders (Pods)
     */
    suspend fun fetchFolders(
        sessionToken: String
    ): Result<List<Folder>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/folders") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }

            if (response.status == HttpStatusCode.OK) {
                val listResponse = response.body<FoldersResponse>()
                listResponse.data
            } else {
                throw Exception("Fetch folders failed: ${response.status.value}")
            }
        }
    }

    /**
     * 7. Fetch Bookmark Stats
     */
    suspend fun fetchBookmarkStats(
        sessionToken: String
    ): Result<BookmarkStats> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/bookmarks/stats") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }

            if (response.status == HttpStatusCode.OK) {
                val statsRes = response.body<StatsResponse>()
                statsRes.data
            } else {
                throw Exception("Fetch stats failed: ${response.status.value}")
            }
        }
    }

    /**
     * 8. Fetch Distinct Tags
     */
    suspend fun fetchTags(
        sessionToken: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/bookmarks/tags") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }

            if (response.status == HttpStatusCode.OK) {
                val tagsRes = response.body<TagsResponse>()
                tagsRes.data
            } else {
                throw Exception("Fetch tags failed: ${response.status.value}")
            }
        }
    }

    fun close() {
        client.close()
    }
}
