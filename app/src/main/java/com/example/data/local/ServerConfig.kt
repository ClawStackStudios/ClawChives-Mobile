package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_config")
data class ServerConfig(
    @PrimaryKey val id: Int = 1,
    val serverUrl: String
)
