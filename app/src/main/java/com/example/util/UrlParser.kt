package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

object UrlParser {
    private val client = OkHttpClient()

    private val titlePattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val metaDescPattern1 = Pattern.compile("<meta[^>]*name=[\"']description[\"'][^>]*content=[\"']([^\"']*)[\"'][^>]*>", Pattern.CASE_INSENSITIVE)
    private val metaDescPattern2 = Pattern.compile("<meta[^>]*content=[\"']([^\"']*)[\"'][^>]*name=[\"']description[\"'][^>]*>", Pattern.CASE_INSENSITIVE)
    private val propertyDescPattern1 = Pattern.compile("<meta[^>]*property=[\"']og:description[\"'][^>]*content=[\"']([^\"']*)[\"'][^>]*>", Pattern.CASE_INSENSITIVE)
    private val propertyDescPattern2 = Pattern.compile("<meta[^>]*content=[\"']([^\"']*)[\"'][^>]*property=[\"']og:description[\"'][^>]*>", Pattern.CASE_INSENSITIVE)

    suspend fun fetchMetadata(url: String): Pair<String?, String?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Pair(null, null)

                val html = response.body?.string() ?: return@withContext Pair(null, null)

                var title: String? = null
                val titleMatcher = titlePattern.matcher(html)
                if (titleMatcher.find()) {
                    title = titleMatcher.group(1)?.trim()
                }

                var description: String? = null
                val matchers = listOf(
                    metaDescPattern1.matcher(html),
                    metaDescPattern2.matcher(html),
                    propertyDescPattern1.matcher(html),
                    propertyDescPattern2.matcher(html)
                )

                for (matcher in matchers) {
                    if (matcher.find()) {
                        description = matcher.group(1)?.trim()
                        break
                    }
                }
                
                // Unescape basic HTML entities
                title = title?.replace("&amp;", "&")?.replace("&#39;", "'")?.replace("&quot;", "\"")
                description = description?.replace("&amp;", "&")?.replace("&#39;", "'")?.replace("&quot;", "\"")

                Pair(title, description)
            }
        } catch (e: Exception) {
            Pair(null, null)
        }
    }
}
