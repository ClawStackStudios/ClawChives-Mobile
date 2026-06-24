package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppConfig::class, FilterState::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun filterStateDao(): FilterStateDao
}
