package com.example.data.remote

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
    val token: String,
    val type: String,
    val createdAt: String,
    val expiresAt: String,
    val user: UserInfo
)

@Serializable
data class UserInfo(
    val uuid: String,
    val username: String
)

@Serializable
data class Bookmark(
    val id: String,
    val url: String,
    val title: String,
    val description: String? = null,
    val favicon: String? = null,
    val tags: List<String> = emptyList(),
    val folderId: String? = null,
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
    val folderId: String? = null,
    val starred: Boolean? = null,
    val archived: Boolean? = null,
    val color: String? = null,
    val jinaUrl: String? = null
)

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
data class BookmarkResponse(
    val success: Boolean,
    val data: Bookmark
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

@Serializable
data class Folder(
    val id: String,
    val name: String,
    val color: String? = null,
    val icon: String? = null,
    val createdAt: String? = null
)

@Serializable
data class FoldersResponse(
    val success: Boolean,
    val data: List<Folder>
)

@Serializable
data class BookmarksResponse(
    val success: Boolean,
    val data: List<Bookmark>
)

@Serializable
data class ValidationErrorDetails(
    val path: String,
    val message: String
)

@Serializable
data class ValidationExceptionResponse(
    val success: Boolean,
    val error: String,
    val details: List<ValidationErrorDetails> = emptyList()
)

@Serializable
data class TagsResponse(
    val success: Boolean,
    val data: List<String>
)
