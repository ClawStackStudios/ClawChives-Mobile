package com.example.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Automates integration tests that confirm state consistency after each change.
 * Validates the harmonious boundary between the mobile client and web server APIs,
 * checking bidirectional validation, fallback logic for version mismatches, and graceful handling.
 */
class StateBoundaryIntegrationTest {

    @Test
    fun `test API client sets version contract headers`() = runBlocking {
        var capturedClientVersion: String? = null
        var capturedAcceptVersion: String? = null
        var capturedAccept: String? = null

        val mockEngine = MockEngine { request ->
            capturedClientVersion = request.headers["X-Client-Version"]
            capturedAcceptVersion = request.headers["Accept-Version"]
            capturedAccept = request.headers["Accept"]
            
            respond(
                content = """{"success":true,"service":"ClawChives","version":"0.0.3.0","mode":"prod","uptime":120.0,"counts":{"bookmarks":0,"folders":0,"agentKeys":0}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ClawChivesClient("http://localhost:8080", null, mockEngine)
        val result = client.getHealth()

        assertTrue(result.isSuccess)
        assertEquals("0.0.3.0", capturedClientVersion)
        assertEquals("0.0.3.0", capturedAcceptVersion)
        assertEquals("application/json", capturedAccept)
    }

    @Test
    fun `test bidirectional validation with graceful fallback for version mismatch and omitted arrays`() = runBlocking {
        val mockEngine = MockEngine { request ->
            // Simulating a newer server response with extra, unknown fields
            // AND omitting historical arrays (e.g. tags is missing)
            // The JSON config should ignore unknown elements and use default values for omitted keys
            val jsonResponse = """
                {
                    "success": true,
                    "data": {
                        "id": "123",
                        "url": "https://example.com",
                        "title": "Example",
                        "createdAt": "2023-01-01T00:00:00Z",
                        "unknown_future_field": "some data",
                        "newly_added_feature": true
                    }
                }
            """.trimIndent()
            
            respond(
                content = jsonResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = ClawChivesClient("http://localhost:8080", null, mockEngine)
        
        // This should not crash despite 'unknown_future_field' not existing in the local Model
        // and 'tags' array being completely omitted
        val result = client.createBookmark("fake-token", BookmarkCreateRequest("https://example.com", "Example"))
        
        assertTrue("Client should gracefully ignore unknown future fields & omitted historical arrays", result.isSuccess)
        val bookmark = result.getOrNull()!!
        assertEquals("123", bookmark.id)
        assertTrue(bookmark.tags.isEmpty())
    }
}
