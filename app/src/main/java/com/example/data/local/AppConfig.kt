package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val serverUrl: String? = null,
    val authToken: String? = null,
    val rawKey: String? = null,
    val theme: String = "SYSTEM"
)
