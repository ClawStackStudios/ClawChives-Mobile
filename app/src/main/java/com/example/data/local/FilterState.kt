package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_state")
data class FilterState(
    @PrimaryKey val contextKey: String, // e.g. "folder_123" or "tab_all"
    val filterStarred: Boolean = false,
    val filterPinned: Boolean = false,
    val filterArchived: Boolean = false,
    val tagFilter: String? = null,
    val sortBy: String = "date-desc"
)
