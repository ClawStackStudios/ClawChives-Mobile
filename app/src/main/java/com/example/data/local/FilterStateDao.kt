package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FilterStateDao {
    @Query("SELECT * FROM filter_state WHERE contextKey = :key")
    suspend fun getFilterState(key: String): FilterState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFilterState(state: FilterState)
}
