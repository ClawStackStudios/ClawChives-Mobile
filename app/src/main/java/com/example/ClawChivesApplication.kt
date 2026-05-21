package com.example

import android.app.Application
import com.example.data.local.AuthPreferences
import com.example.data.repository.AuthRepository

class ClawChivesApplication : Application() {
    
    lateinit var authPreferences: AuthPreferences
        private set
    lateinit var authRepository: AuthRepository
        private set
        
    override fun onCreate() {
        super.onCreate()
        authPreferences = AuthPreferences(this)
        authRepository = AuthRepository(authPreferences)
    }
}
