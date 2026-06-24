package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository

class ClawChivesApplication : Application() {
    
    lateinit var themePreferences: com.example.ui.theme.ThemePreferences
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var authRepository: AuthRepository
        private set
        
    override fun onCreate() {
        super.onCreate()
        themePreferences = com.example.ui.theme.ThemePreferences(this)
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "clawchives_database"
        )
        .fallbackToDestructiveMigration(true)
        .build()
        
        authRepository = AuthRepository(database.appConfigDao())
    }
}
