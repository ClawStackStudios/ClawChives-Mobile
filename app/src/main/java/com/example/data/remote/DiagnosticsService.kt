package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

data class DiagnosticResult(
    val endpoint: String,
    val success: Boolean,
    val message: String,
    val dataSize: Int? = null,
    val parityValid: Boolean = false
)

class DiagnosticsService(private val client: ClawChivesClient) {

    suspend fun runDiagnostics(sessionToken: String): List<DiagnosticResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<DiagnosticResult>()

        // 1. Test Folders (Pods) Endpoint
        try {
            val foldersResult = client.fetchFolders(sessionToken)
            if (foldersResult.isSuccess) {
                val folders = foldersResult.getOrNull() ?: emptyList()
                results.add(
                    DiagnosticResult(
                        endpoint = "GET /api/folders",
                        success = true,
                        message = "Success",
                        dataSize = folders.size,
                        parityValid = true // Basic parity: it parses into Folder list
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        endpoint = "GET /api/folders",
                        success = false,
                        message = foldersResult.exceptionOrNull()?.message ?: "Unknown error"
                    )
                )
            }
        } catch (e: Exception) {
            results.add(DiagnosticResult("GET /api/folders", false, e.message ?: "Exception"))
        }

        // 2. Test Bookmarks (Pinchmarks) Endpoint
        try {
            val bookmarksResult = client.fetchBookmarks(sessionToken, page = 1, limit = 10)
            if (bookmarksResult.isSuccess) {
                val bookmarks = bookmarksResult.getOrNull() ?: emptyList()
                results.add(
                    DiagnosticResult(
                        endpoint = "GET /api/bookmarks",
                        success = true,
                        message = "Success",
                        dataSize = bookmarks.size,
                        parityValid = true
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        endpoint = "GET /api/bookmarks",
                        success = false,
                        message = bookmarksResult.exceptionOrNull()?.message ?: "Unknown error"
                    )
                )
            }
        } catch (e: Exception) {
            results.add(DiagnosticResult("GET /api/bookmarks", false, e.message ?: "Exception"))
        }

        // 3. Test Tags Endpoint (Validating BOUNDARY.md invariant)
        // Ensure data shape parity (primitive string list)
        try {
            val tagsResult = client.fetchTags(sessionToken)
            if (tagsResult.isSuccess) {
                val tags = tagsResult.getOrNull() ?: emptyList()
                
                // BOUNDARY.md constraint validation: must be list of strings, and shouldn't have trailing spaces
                val isValidShape = tags.all { it == it.trim() && it.isNotBlank() }
                
                if (isValidShape) {
                    results.add(
                        DiagnosticResult(
                            endpoint = "GET /api/bookmarks/tags",
                            success = true,
                            message = "Success, primitive list valid",
                            dataSize = tags.size,
                            parityValid = true
                        )
                    )
                } else {
                    results.add(
                        DiagnosticResult(
                            endpoint = "GET /api/bookmarks/tags",
                            success = true,
                            message = "Data shape mismatch or un-trimmed tags found",
                            dataSize = tags.size,
                            parityValid = false
                        )
                    )
                    Log.w("Diagnostics", "Tag data shape invalid. Tags: $tags")
                }
            } else {
                results.add(
                    DiagnosticResult(
                        endpoint = "GET /api/bookmarks/tags",
                        success = false,
                        message = tagsResult.exceptionOrNull()?.message ?: "Unknown error"
                    )
                )
            }
        } catch (e: Exception) {
            results.add(DiagnosticResult("GET /api/bookmarks/tags", false, e.message ?: "Exception"))
        }

        results
    }
}
