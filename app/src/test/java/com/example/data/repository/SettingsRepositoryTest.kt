package com.example.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppConfigDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var appConfigDao: AppConfigDao
    private lateinit var settingsRepository: RoomSettingsRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        appConfigDao = database.appConfigDao()
        settingsRepository = RoomSettingsRepository(appConfigDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveServerUrl_withValidUrl_savesToDatabase() = runTest {
        val validUrl = "https://example.com"
        
        settingsRepository.saveServerUrl(validUrl)
        
        val savedUrl = settingsRepository.serverUrl.first()
        assertEquals(validUrl, savedUrl)
    }

    @Test
    fun saveServerUrl_withTrailingSlash_stripsSlashBeforeSaving() = runTest {
        val urlWithSlash = "https://example.com/"
        
        settingsRepository.saveServerUrl(urlWithSlash)
        
        val savedUrl = settingsRepository.serverUrl.first()
        assertEquals("https://example.com", savedUrl)
    }

    @Test
    fun saveServerUrl_withBlankUrl_throwsIllegalArgumentException() = runTest {
        var thrown = false
        try {
            settingsRepository.saveServerUrl("   ")
        } catch (e: IllegalArgumentException) {
            thrown = true
            assertEquals("Server URL cannot be blank", e.message)
        }
        assertTrue("Expected IllegalArgumentException to be thrown", thrown)
    }

    @Test
    fun saveServerUrl_withoutHttp_throwsIllegalArgumentException() = runTest {
        var thrown = false
        try {
            settingsRepository.saveServerUrl("example.com")
        } catch (e: IllegalArgumentException) {
            thrown = true
            assertEquals("Server URL must start with http:// or https://", e.message)
        }
        assertTrue("Expected IllegalArgumentException to be thrown", thrown)
    }
    
    @Test
    fun saveTheme_savesThemeToDatabase() = runTest {
        settingsRepository.saveTheme("DARK")
        
        val savedTheme = settingsRepository.theme.first()
        assertEquals("DARK", savedTheme)
    }

    @Test
    fun clearSettings_resetsServerUrlAndTheme() = runTest {
        settingsRepository.saveServerUrl("https://example.com")
        settingsRepository.saveTheme("DARK")
        
        settingsRepository.clearSettings()
        
        val savedUrl = settingsRepository.serverUrl.first()
        val savedTheme = settingsRepository.theme.first()
        
        assertEquals(null, savedUrl)
        assertEquals("SYSTEM", savedTheme)
    }
}
