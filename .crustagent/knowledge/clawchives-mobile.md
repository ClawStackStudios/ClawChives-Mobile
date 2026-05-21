# 🦞 ClawChives Android Native Library: Architectural Specification

This document is a technical blueprint for scaffolding a secure, high-performance native Kotlin library (`ClawChivesClient`) to connect an Android application to a sovereign **ClawChives** server over LAN or Cloudflare.

Use this specification directly in your LLM/Agent generation prompt in Google AI Studio to get highly accurate, complete, and robust code.

---

## 📐 1. Architecture & Network Topology

The library utilizes the **Repository Pattern** to decouple networking layers from UI states, ensuring offline-first compatibility and clean state management.

```
 ┌──────────────────────┐
 │    UI / ViewModel    │ (Jetpack Compose / standard Views)
 └──────────────────────┘
            │
            ▼
 ┌──────────────────────┐
 │ ClawChivesRepository │ (Orchestrates cache vs network, handles token injection)
 └──────────────────────┘
            │
            ▼
 ┌──────────────────────┐
 │   HTTP API Client    │ (Ktor Client with Coroutines + kotlinx.serialization)
 └──────────────────────┘
```

### Protocol Constraints & Network Seams:
* **Base URL:** Must support local IP addresses with custom ports (`http://192.168.x.x:4646`) as well as secure HTTPS Cloudflare Tunnel domains (`https://*.trycloudflare.com`).
* **Authentication Bearer:** All data endpoints require an `Authorization` header with the format: `Bearer api-<token_hash>`.
* **Serialization:** Pure Kotlin standard `kotlinx.serialization` (JSON parsing).

---

## 🔐 2. Cryptographic Handshake Protocol (Option B)

To protect the master identity key, **the server never receives the `hu-` key in plaintext.** The Android library must perform local SHA-256 pre-hashing before sending authentication requests.

```
  ┌────────────────────────────────────────────────────────┐
  │ 1. User inputs raw "hu-..." Master Key                 │
  └────────────────────────────────────────────────────────┘
                              │
                              ▼
  ┌────────────────────────────────────────────────────────┐
  │ 2. Compute SHA-256 hash of the full "hu-..." string    │
  └────────────────────────────────────────────────────────┘
                              │
                              ▼
  ┌────────────────────────────────────────────────────────┐
  │ 3. Convert hash to 64-char lowercase Hex String        │
  └────────────────────────────────────────────────────────┘
                              │
                              ▼
  ┌────────────────────────────────────────────────────────┐
  │ 4. Send Hex String as "keyHash" to POST /api/auth/token│
  └────────────────────────────────────────────────────────┘
```

### Kotlin Cryptographic Utility implementation:
Ensure the Agent implements the SHA-256 pre-hash correctly:

```kotlin
import java.security.MessageDigest

object ClawCrypto {
    /**
     * Hashes a plaintext hu- key using SHA-256 and returns a lowercase 64-char hex string.
     */
    fun hashHumanKey(rawKey: String): String {
        require(rawKey.startsWith("hu-")) { "Key must begin with 'hu-' prefix" }
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawKey.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
```

---

## 📦 3. Data Schema & Models (Kotlin Data Classes)

These models strictly map to the Zod validation schemas and SQLite row parsers implemented on the ClawChives backend.

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val success: Boolean,
    val service: String,
    val version: String,
    val mode: String,
    val uptime: Double,
    val counts: DatabaseCounts
)

@Serializable
data class DatabaseCounts(
    val bookmarks: Int,
    val folders: Int,
    val agentKeys: Int
)

@Serializable
data class TokenRequest(
    val type: String = "human",
    val keyHash: String
)

@Serializable
data class TokenResponse(
    val success: Boolean,
    val data: SessionData? = null,
    val error: String? = null
)

@Serializable
data class SessionData(
    val token: String, // The session "api-xxxx..." token used for Bearer auth
    val type: String,  // "human"
    val createdAt: String,
    val expiresAt: String,
    val user: UserInfo
)

@Serializable
data class UserInfo(
    val uuid: String,
    val username: String
)

// ─── PINCHMARK (BOOKMARK) MODELS ─────────────────────────────────────────────

@Serializable
data class Bookmark(
    val id: String,
    val url: String,
    val title: String,
    val description: String? = null,
    val favicon: String? = null,
    val tags: List<String> = emptyList(),
    val folderId: String? = null, // Linked Folder/Pod ID
    val starred: Boolean = false,
    val archived: Boolean = false,
    val color: String? = null,
    val createdAt: String,
    val jinaUrl: String? = null
)

@Serializable
data class BookmarkCreateRequest(
    val url: String,
    val title: String,
    val description: String? = null,
    val favicon: String? = null,
    val tags: List<String> = emptyList(),
    val folderId: String? = null, // Pass Folder UUID to nest this inside a Pod
    val starred: Boolean? = null,
    val archived: Boolean? = null,
    val color: String? = null,
    val jinaUrl: String? = null
)

@Serializable
data class BookmarkUpdateRequest(
    val url: String? = null,
    val title: String? = null,
    val description: String? = null,
    val favicon: String? = null,
    val tags: List<String>? = null,
    val folderId: String? = null,
    val starred: Boolean? = null,
    val archived: Boolean? = null,
    val color: String? = null,
    val jinaUrl: String? = null
)

@Serializable
data class BookmarkStats(
    val total: Int,
    val starred: Int,
    val archived: Int
)

@Serializable
data class StatsResponse(
    val success: Boolean,
    val data: BookmarkStats
)

@Serializable
data class BookmarksResponse(
    val success: Boolean,
    val data: List<Bookmark>
)

// ─── FOLDER (POD) MODELS ─────────────────────────────────────────────────────

@Serializable
data class Folder(
    val id: String,
    val name: String,
    val parentId: String? = null, // Support nested folders/sub-pods
    val color: String = "#06b6d4",
    val createdAt: String
)

@Serializable
data class FolderCreateRequest(
    val name: String,
    val parentId: String? = null,
    val color: String? = null // Format: "#FFFFFF"
)

@Serializable
data class FolderUpdateRequest(
    val name: String? = null,
    val parentId: String? = null,
    val color: String? = null
)

@Serializable
data class FoldersResponse(
    val success: Boolean,
    val data: List<Folder>
)

@Serializable
data class FolderSingleResponse(
    val success: Boolean,
    val data: Folder
)

// ─── TAGS MODELS ─────────────────────────────────────────────────────────────

@Serializable
data class TagsResponse(
    val success: Boolean,
    val data: List<String>
)
```

---

## 📡 4. REST Endpoints Specification

ClawChives Android application will query the following unified API routes:

| HTTP Method | Route Path | Auth Required | Description |
|:---|:---|:---:|:---|
| `GET` | `/api/health` | No | Connectivity & ping check. |
| `POST` | `/api/auth/token` | No | Login. Submits local `keyHash` and receives `api-` token. |
| `GET` | `/api/bookmarks` | Yes | Retrieves all bookmarks. Supports query params: `starred`, `archived`, `folderId`, `search`, `limit`, `page`. |
| `POST` | `/api/bookmarks` | Yes | Creates a new bookmark. Validates schema body. |
| `PUT` | `/api/bookmarks/{id}` | Yes | Updates a specific bookmark fields. |
| `DELETE` | `/api/bookmarks/{id}` | Yes | Deletes a specific bookmark. |
| `GET` | `/api/bookmarks/stats` | Yes | Fast query for totals (`total`, `starred`, `archived`). |
| `GET` | `/api/bookmarks/tags` | Yes | Returns unique, distinct tag list `["tech", "rust"]`. |
| `GET` | `/api/folders` | Yes | Fetches flat array of all user folders/pods. |
| `POST` | `/api/folders` | Yes | Creates a new folder/pod. |
| `PUT` | `/api/folders/{id}` | Yes | Renames or changes color of a folder/pod. |
| `DELETE` | `/api/folders/{id}` | Yes | Deletes a folder/pod. |

---

## 🛠️ 5. Coroutine-Based Ktor Networking Service

Here is the complete production-ready network client implementation using the **Ktor HTTP Client engine**.

```kotlin
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

class ClawChivesClient(
    private val baseUrl: String // Format: http://192.168.1.150:4646 or https://domain.com
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
                // Ensure proper trailing slash handling
                val base = baseUrl.removeSuffix("/")
                takeFrom(base)
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
            val requestBody = TokenRequest(type = "human", keyHash = keyHash)
            
            val response: HttpResponse = client.post("api/auth/token") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status == HttpStatusCode.Created) {
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

    // ─── BOOKMARKS CLIENT ────────────────────────────────────────────────────

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
     * 4. Create Bookmark (Pinchmark)
     */
    suspend fun createBookmark(
        sessionToken: String,
        bookmarkRequest: BookmarkCreateRequest
    ): Result<Bookmark> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.post("api/bookmarks") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
                contentType(ContentType.Application.Json)
                setBody(bookmarkRequest)
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                val res = response.body<Map<String, Bookmark>>()
                res["data"] ?: throw Exception("Invalid body response structure")
            } else {
                throw Exception("Create bookmark failed: ${response.status.value}")
            }
        }
    }

    /**
     * 5. Fetch Bookmark Stats
     */
    suspend fun fetchBookmarkStats(sessionToken: String): Result<BookmarkStats> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/bookmarks/stats") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<StatsResponse>().data
            } else {
                throw Exception("Fetch stats failed: ${response.status.value}")
            }
        }
    }

    // ─── FOLDERS (PODS) CLIENT ───────────────────────────────────────────────

    /**
     * 6. Fetch Folders
     */
    suspend fun fetchFolders(sessionToken: String): Result<List<Folder>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/folders") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<FoldersResponse>().data
            } else {
                throw Exception("Fetch folders failed: ${response.status.value}")
            }
        }
    }

    /**
     * 7. Create Folder
     */
    suspend fun createFolder(
        sessionToken: String,
        folderRequest: FolderCreateRequest
    ): Result<Folder> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.post("api/folders") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
                contentType(ContentType.Application.Json)
                setBody(folderRequest)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<FolderSingleResponse>().data
            } else {
                throw Exception("Create folder failed: ${response.status.value}")
            }
        }
    }

    /**
     * 8. Update Folder
     */
    suspend fun updateFolder(
        sessionToken: String,
        folderId: String,
        folderRequest: FolderUpdateRequest
    ): Result<Folder> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.put("api/folders/$folderId") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
                contentType(ContentType.Application.Json)
                setBody(folderRequest)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<FolderSingleResponse>().data
            } else {
                throw Exception("Update folder failed: ${response.status.value}")
            }
        }
    }

    /**
     * 9. Delete Folder
     */
    suspend fun deleteFolder(sessionToken: String, folderId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.delete("api/folders/$folderId") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }
            response.status == HttpStatusCode.OK
        }
    }

    // ─── TAGS CLIENT ─────────────────────────────────────────────────────────

    /**
     * 10. Fetch All Tags
     */
    suspend fun fetchTags(sessionToken: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = client.get("api/bookmarks/tags") {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<TagsResponse>().data
            } else {
                throw Exception("Fetch tags failed: ${response.status.value}")
            }
        }
    }

    fun close() {
        client.close()
    }
}
```

---

## 🔒 6. Security Invariants for Google AI Studio Agent

When scaffolding the Android client app, the code generator agent MUST strictly follow these rules:

1. **Cleartext Traffic Policy:** If the server is private HTTP over LAN, the agent must generate a `res/xml/network_security_config.xml` file whitelist allowing private domains/IP ranges (`192.168.0.0/16`, `10.0.0.0/8`), rather than globally enabling cleartext traffic (`cleartextTrafficPermitted="true"` on all domains).
2. **Encrypted Storage:** The `api-` session token represents active user authorization and must **never** be stored in cleartext XML SharedPreferences. Instruct the agent to use Jetpack's `EncryptedSharedPreferences` for token persistence.
3. **Graceful Revocation (401 Handler):** The Ktor client should be set up with an HTTP interceptor or custom exception handling that intercepts `HttpStatusCode.Unauthorized` (401), automatically flushes the local encrypted storage, and forces a redirect to the App's Login screen to prevent silent failures.

---

## ⚡ 7. Android Implementation Stability Upgrades (Zod & Scroll Invariants)

To guarantee 100% stable integration between the Android application and the strict backend Zod verification layers, the following architectural upgrades must be strictly followed in any client codebases:

### 🧩 7a. Zod-Compliant Kotlin DTO Sanitizers
Ktor's `encodeDefaults = true` configuration serializes uninstantiated optional request fields as `null` (e.g. `"description": null`). Because the backend Zod schemas (`/src/server/validation/schemas.ts`) define `description` and `favicon` as optional but **not** nullable, sending explicit `null` fields triggers a `400 Bad Request` validation error.

To neutralize this seam, all Kotlin requests must be sanitized prior to transit using the following rules:
* Any optional non-nullable string fields (`description`, `favicon`) must default to `""` (empty string) if they are `null` or blank.
* Any checked Boolean fields for Jina translation (`isJina`) must explicitly construct the target wrapper URL: `jinaUrl = if (isJina) "https://r.jina.ai/$finalUrl" else null` instead of passing `"true"`.
* Edit actions must retrieve and preserve existing properties (`color`, `favicon`) from `bookmarkToEdit` rather than defaulting to `null` to avoid wiping out existing server values.

#### Implementation Pattern:
```kotlin
fun BookmarkCreateRequest.sanitize(): BookmarkCreateRequest {
    return this.copy(
        description = if (description.isNullOrBlank()) "" else description,
        favicon = if (favicon.isNullOrBlank()) "" else favicon,
        folderId = if (folderId.isNullOrBlank()) null else folderId,
        color = if (color.isNullOrBlank()) null else color,
        jinaUrl = if (jinaUrl.isNullOrBlank()) null else jinaUrl,
        starred = starred ?: false,
        archived = archived ?: false
    )
}

fun BookmarkUpdateRequest.sanitize(): BookmarkUpdateRequest {
    return this.copy(
        description = if (description.isNullOrBlank()) "" else description,
        favicon = if (favicon.isNullOrBlank()) "" else favicon,
        folderId = if (folderId.isNullOrBlank()) null else folderId,
        color = if (color.isNullOrBlank()) null else color,
        jinaUrl = if (jinaUrl.isNullOrBlank()) null else jinaUrl,
        starred = starred ?: false,
        archived = archived ?: false
    )
}
```

### 📈 7b. Jetpack Compose Infinite Scroll Pagination Pattern
To prevent heavy network payloads and UI frame-rate stutters as user collections grow to thousands of pinchmarks, load pages dynamically using Compose's lightweight scroll state telemetry.

#### ViewModel State Management:
```kotlin
sealed interface DashboardState {
    object Loading : DashboardState
    data class Success(
        val bookmarks: List<Bookmark>,
        val folders: List<Folder>,
        val isLoadingMore: Boolean = false,
        val isLastPage: Boolean = false
    ) : DashboardState
    data class Error(val message: String) : DashboardState
}

// Inside ViewModel:
private val loadedBookmarks = mutableListOf<Bookmark>()
private var currentPage = 1
private var isLastPage = false
private var isCurrentlyLoading = false
private val pageSize = 50

fun loadBookmarks(reset: Boolean = true) {
    if (isCurrentlyLoading) return
    if (!reset && isLastPage) return
    isCurrentlyLoading = true

    viewModelScope.launch {
        if (reset) {
            currentPage = 1
            isLastPage = false
            loadedBookmarks.clear()
            _uiState.value = DashboardState.Loading
        } else {
            val currentState = _uiState.value
            if (currentState is DashboardState.Success) {
                _uiState.value = currentState.copy(isLoadingMore = true)
            }
        }

        try {
            val client = ApiClient.getCurrentClient()
            val token = ApiClient.authToken ?: throw Exception("Not logged in")
            val result = client.fetchBookmarks(token, page = currentPage, limit = pageSize)
            
            if (result.isSuccess) {
                val newBookmarks = result.getOrThrow()
                loadedBookmarks.addAll(newBookmarks)
                if (newBookmarks.size < pageSize) isLastPage = true else currentPage++

                _uiState.value = DashboardState.Success(
                    bookmarks = loadedBookmarks.toList(),
                    folders = cachedFolders,
                    isLoadingMore = false,
                    isLastPage = isLastPage
                )
            }
        } finally {
            isCurrentlyLoading = false
        }
    }
}
```

#### Jetpack Compose UI Trigger:
Use Compose's `derivedStateOf` to monitor the scroll position relative to the end of the loaded list, triggering lazy fetches without redundant UI recomposition.
```kotlin
val listState = rememberLazyListState()
val shouldLoadMore = remember {
    derivedStateOf {
        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            ?: return@derivedStateOf false
        lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
    }
}

LaunchedEffect(shouldLoadMore.value) {
    if (shouldLoadMore.value && uiState is DashboardState.Success) {
        viewModel.loadBookmarks(reset = false)
    }
}

// In LazyColumn:
LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize()
) {
    items(state.bookmarks) { bookmark ->
        PinchmarkCard(bookmark = bookmark, ...)
    }
    if (state.isLoadingMore) {
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyanAccent)
            }
        }
    }
}
```

---
### 🎨 7c. High-Fidelity Custom Navigation Drawer
To maintain visual and functional parity with the web dashboard's `Sidebar.tsx` and `SidebarNav.tsx`, the Android navigation drawer uses a styled Compose custom layout inside `ModalNavigationDrawer`:
* **Interactive Brand Header:** Displays the bold cyan application title ("ClawChives") accompanied by the lobster emoji (`🦞`) and a smaller copyright subtitle ("ClawStack Studios©™").
* **Granular Search Integration:** Includes an `OutlinedTextField` inside the drawer that directly binds to the ViewModel's `searchQuery` and triggers live, debounced network queries upon keystroke changes, complete with clear (`X`) buttons.
* **Premium Colored Navigation Tabs:** Built using a customized `DrawerNavItem` which renders custom background highlights (12% alpha) and distinct accent text colors for each system tab (Cyan for All, Amber/Warning for Starred, Teal/Sky for Tags, and Red for Archived), along with circular trailing count badges.
* **Dynamic Pods (Folders) Listing:** Dynamically parses custom Hex colors from the API to draw custom colored canvas dots (`Canvas` circular shapes) beside each user folder/pod item.
* **Dynamic TopAppBar Titles:** Seamlessly tracks active drawer state filters to update the top scaffold bar's title depending on whether the user is browsing All, Starred, Archived, or a specific Pod.

---
*Maintained by CrustAgent©™*
