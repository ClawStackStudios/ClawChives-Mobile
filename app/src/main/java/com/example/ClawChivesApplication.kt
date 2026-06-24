package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.AuthPreferences
import com.example.data.repository.AuthRepository

class ClawChivesApplication : Application() {
    
    lateinit var authPreferences: AuthPreferences
        private set
    lateinit var themePreferences: com.example.ui.theme.ThemePreferences
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var authRepository: AuthRepository
        private set
        
    override fun onCreate() {
        super.onCreate()
        authPreferences = AuthPreferences(this)
        themePreferences = com.example.ui.theme.ThemePreferences(this)
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "clawchives_database"
        ).build()
        authRepository = AuthRepository(authPreferences, database.serverConfigDao())
    }
}
