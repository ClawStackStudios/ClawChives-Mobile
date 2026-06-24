package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getConfigSync(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)

    @Query("DELETE FROM app_config WHERE id = 1")
    suspend fun deleteConfig()

    @Query("UPDATE app_config SET theme = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)
    
    @Query("UPDATE app_config SET serverUrl = :serverUrl, authToken = :authToken, rawKey = :rawKey WHERE id = 1")
    suspend fun updateAuth(serverUrl: String?, authToken: String?, rawKey: String?)
}
