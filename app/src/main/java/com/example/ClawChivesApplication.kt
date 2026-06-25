package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository

import com.example.data.repository.SettingsRepository
import com.example.data.repository.RoomSettingsRepository

class ClawChivesApplication : Application() {
    
    lateinit var database: AppDatabase
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
        
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "clawchives_database"
        )
        .fallbackToDestructiveMigration(true)
        .build()
        
        settingsRepository = RoomSettingsRepository(database.appConfigDao())
        authRepository = AuthRepository(database.appConfigDao())
    }
}
