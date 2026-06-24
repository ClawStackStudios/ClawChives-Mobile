package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_config WHERE id = 1")
    fun getServerConfig(): Flow<ServerConfig?>

    @Query("SELECT * FROM server_config WHERE id = 1")
    suspend fun getServerConfigSync(): ServerConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ServerConfig)
    
    @Query("DELETE FROM server_config WHERE id = 1")
    suspend fun deleteConfig()
}
