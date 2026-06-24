package com.example.ui.feature.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.ApiClient
import com.example.data.remote.Bookmark
import com.example.data.remote.BookmarkCreateRequest
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.data.remote.BookmarkUpdateRequest
import com.example.data.remote.BookmarkStats
import com.example.data.remote.Folder
import com.example.data.remote.FolderCreateRequest

sealed interface DashboardState {
    object Loading : DashboardState
    data class Success(
        val bookmarks: List<Bookmark>,
        val folders: List<Folder>,
        val stats: BookmarkStats? = null,
        val tagsCount: Int = 0,
        val selectedFilter: String = "all",
        val selectedFolderId: String? = null,
        val searchQuery: String = "",
        val sortBy: String = "date-desc",
        val filterStarred: Boolean = false,
        val filterPinned: Boolean = false,
        val filterArchived: Boolean = false,
        val tagFilter: String? = null,
        val allTags: List<String> = emptyList(),
        val isLoadingMore: Boolean = false,
        val isLastPage: Boolean = false
    ) : DashboardState
    data class Error(val message: String) : DashboardState
}

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private var cachedFolders = emptyList<Folder>()
    private val loadedBookmarks = mutableListOf<Bookmark>()
    private var currentPage = 1
    private var isLastPage = false
    private var isCurrentlyLoading = false
    private var pageSize = 50

    var selectedFilter = "all"
        private set
    var selectedFolderId: String? = null
        private set
    var searchQuery = ""
        private set

    var sortBy = "date-desc"
        private set
    var filterStarred = false
        private set
    var filterPinned = false
        private set
    var filterArchived = false
        private set
    var tagFilter: String? = null
        private set

    private var cachedStats: BookmarkStats? = null
    private var cachedTagsCount = 0
    private var cachedTags = emptyList<String>()
    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadFilterState(selectedFilter, selectedFolderId)
        loadBookmarks(reset = true)
        viewModelScope.launch {
            authRepository.sessionRefreshed.collect {
                loadBookmarks(reset = true)
            }
        }
    }

    private fun saveFilterState() {
        val prefix = if (selectedFolderId != null) "folder_$selectedFolderId" else "tab_$selectedFilter"
        prefs.edit()
            .putBoolean("${prefix}_filterStarred", filterStarred)
            .putBoolean("${prefix}_filterPinned", filterPinned)
            .putBoolean("${prefix}_filterArchived", filterArchived)
            .putString("${prefix}_tagFilter", tagFilter)
            .putString("${prefix}_sortBy", sortBy)
            .apply()
    }

    private fun loadFilterState(newFilter: String, newFolderId: String?) {
        val prefix = if (newFolderId != null) "folder_$newFolderId" else "tab_$newFilter"
        filterStarred = prefs.getBoolean("${prefix}_filterStarred", false)
        filterPinned = prefs.getBoolean("${prefix}_filterPinned", false)
        filterArchived = prefs.getBoolean("${prefix}_filterArchived", false)
        tagFilter = prefs.getString("${prefix}_tagFilter", null)
        sortBy = prefs.getString("${prefix}_sortBy", "date-desc") ?: "date-desc"
    }

    fun setFilter(filter: String) {
        if (selectedFilter == filter && selectedFolderId == null) return
        saveFilterState()
        selectedFilter = filter
        selectedFolderId = null
        loadFilterState(selectedFilter, selectedFolderId)
        loadBookmarks(reset = true)
    }

    fun selectFolder(folderId: String?) {
        if (selectedFolderId == folderId) return
        saveFilterState()
        selectedFolderId = folderId
        selectedFilter = "all" // reset to all when inside folder
        loadFilterState(selectedFilter, selectedFolderId)
        loadBookmarks(reset = true)
    }

    fun setSearchQuery(query: String) {
        if (searchQuery == query) return
        searchQuery = query
        loadBookmarks(reset = true)
    }

    fun setSortBy(sort: String) {
        if (sortBy == sort) return
        sortBy = sort
        saveFilterState()
        updateUIState() // sorting is local
    }

    fun setFilterStatus(starred: Boolean, pinned: Boolean, archived: Boolean) {
        filterStarred = starred
        filterPinned = pinned
        filterArchived = archived
        saveFilterState()
        loadBookmarks(reset = true)
    }

    fun setTagFilter(tag: String?) {
        tagFilter = tag
        saveFilterState()
        loadBookmarks(reset = true)
    }

    private fun updateUIState(forceLoadingMoreFalse: Boolean = false) {
        val currentState = (_uiState.value as? DashboardState.Success)
        
        val filtered = loadedBookmarks.filter { bookmark ->
            val matchesFolder = selectedFolderId == null || bookmark.folderId == selectedFolderId
            
            val matchesFilter = when (selectedFilter) {
                "starred" -> bookmark.starred == true
                "archived" -> bookmark.archived == true
                "tags" -> bookmark.tags.isNotEmpty() && bookmark.archived != true
                else -> bookmark.archived != true // Usually 'all' excludes archived unless 'archived' is selected
            }

            val matchesHeaderFilter = if (!filterStarred && !filterPinned && !filterArchived && tagFilter == null) {
                true
            } else {
                val s = if (filterStarred) bookmark.starred == true else true
                val p = if (filterPinned) bookmark.pinned == true else true
                val a = if (filterArchived) bookmark.archived == true else true
                val t = if (tagFilter != null) bookmark.tags.contains(tagFilter) else true
                s && p && a && t
            }
            
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase()
                (bookmark.title?.lowercase()?.contains(q) == true) ||
                (bookmark.url.lowercase().contains(q)) ||
                (bookmark.description?.lowercase()?.contains(q) == true) ||
                bookmark.tags.any { it.lowercase().contains(q) }
            }
            
            matchesFolder && matchesFilter && matchesHeaderFilter && matchesSearch
        }

        val sorted = when (sortBy) {
            "date-desc" -> filtered.sortedByDescending { it.createdAt }
            "date-asc" -> filtered.sortedBy { it.createdAt }
            "name-asc" -> filtered.sortedBy { (it.title?.takeIf { it.isNotBlank() } ?: it.url).lowercase() }
            "name-desc" -> filtered.sortedByDescending { (it.title?.takeIf { it.isNotBlank() } ?: it.url).lowercase() }
            else -> filtered
        }

        _uiState.value = DashboardState.Success(
            bookmarks = sorted,
            folders = cachedFolders,
            stats = cachedStats,
            tagsCount = cachedTagsCount,
            selectedFilter = selectedFilter,
            selectedFolderId = selectedFolderId,
            searchQuery = searchQuery,
            sortBy = sortBy,
            filterStarred = filterStarred,
            filterPinned = filterPinned,
            filterArchived = filterArchived,
            tagFilter = tagFilter,
            allTags = cachedTags,
            isLoadingMore = if (forceLoadingMoreFalse) false else (currentState?.isLoadingMore ?: false),
            isLastPage = isLastPage
        )
    }

    fun loadBookmarks(reset: Boolean = true) {
        if (isCurrentlyLoading && !reset) return
        if (!reset && isLastPage) return

        isCurrentlyLoading = true
        if (reset) {
            loadJob?.cancel() // Cancel active fetches when resetting
        }
        
        loadJob = viewModelScope.launch {
            if (reset) {
                currentPage = 1
                isLastPage = false
                loadedBookmarks.clear()
                _uiState.value = DashboardState.Loading
            } else {
                val currentState = _uiState.value
                if (currentState is DashboardState.Success) {
                    _uiState.value = currentState.copy(isLoadingMore = true)
                }
            }

            try {
                val client = ApiClient.getCurrentClient()
                val token = ApiClient.authToken ?: throw Exception("Not logged in")
                
                if (reset) {
                    // Clear cached state to prevent ghost tags from previous server connection
                    cachedFolders = emptyList()
                    cachedTags = emptyList()
                    cachedTagsCount = 0
                    cachedStats = null

                    val foldersResult = client.fetchFolders(token)
                    if (foldersResult.isSuccess) {
                        cachedFolders = foldersResult.getOrThrow()
                    }
                    
                    val statsResult = client.fetchBookmarkStats(token)
                    if (statsResult.isSuccess) {
                        cachedStats = statsResult.getOrThrow()
                    }

                    val tagsResult = client.fetchTags(token)
                    if (tagsResult.isSuccess) {
                        val tags = tagsResult.getOrThrow()
                        cachedTagsCount = tags.size
                        cachedTags = tags
                    }
                }

                val apiStarred = if (filterStarred || selectedFilter == "starred") true else null
                val apiArchived = if (filterArchived || selectedFilter == "archived") true else null
                val apiSearch = searchQuery.takeIf { it.isNotBlank() }
                
                val result = client.fetchBookmarks(
                    sessionToken = token,
                    starred = apiStarred,
                    archived = apiArchived,
                    folderId = selectedFolderId,
                    search = apiSearch,
                    tag = tagFilter,
                    page = currentPage,
                    limit = pageSize
                )
                
                if (result.isSuccess) {
                    val newBookmarks = result.getOrThrow()
                    // Filter duplicates in case of overlap on refresh
                    val newIds = newBookmarks.map { it.id }.toSet()
                    if (reset) {
                        loadedBookmarks.clear()
                    } else {
                        loadedBookmarks.removeAll { it.id in newIds }
                    }
                    loadedBookmarks.addAll(newBookmarks)

                    if (newBookmarks.size < pageSize) {
                        isLastPage = true
                    } else {
                        currentPage++
                    }

                    updateUIState(forceLoadingMoreFalse = true)
                } else {
                    if (reset) {
                        _uiState.value = DashboardState.Error(result.exceptionOrNull()?.message ?: "Failed to load bookmarks")
                    } else {
                        val currentState = _uiState.value
                        if (currentState is DashboardState.Success) {
                            _uiState.value = currentState.copy(isLoadingMore = false)
                        }
                    }
                }
            } catch (e: Exception) {
                if (reset) {
                    _uiState.value = DashboardState.Error(e.message ?: "Failed to load bookmarks")
                } else {
                    val currentState = _uiState.value
                    if (currentState is DashboardState.Success) {
                        _uiState.value = currentState.copy(isLoadingMore = false)
                    }
                }
            } finally {
                isCurrentlyLoading = false
            }
        }
    }

    fun updateFolder(folderId: String, request: com.example.data.remote.FolderUpdateRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val client = ApiClient.getCurrentClient()
                val token = ApiClient.authToken ?: throw Exception("Not logged in")
                val result = client.updateFolder(token, folderId, request)
                if (result.isSuccess) {
                    onSuccess()
                    loadBookmarks(reset = true)
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to update folder")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update folder")
            }
        }
    }

    fun deleteFolder(folderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val client = ApiClient.getCurrentClient()
                val token = ApiClient.authToken ?: throw Exception("Not logged in")
                val result = client.deleteFolder(token, folderId)
                if (result.isSuccess) {
                    // Cascading Unassociation
                    cachedFolders = cachedFolders.filter { it.id != folderId }
                    val updatedBookmarks = loadedBookmarks.map { 
                        if (it.folderId == folderId) it.copy(folderId = null) else it 
                    }
                    loadedBookmarks.clear()
                    loadedBookmarks.addAll(updatedBookmarks)
                    if (selectedFolderId == folderId) {
                        selectedFolderId = null
                        selectedFilter = "all"
                    }
                    updateUIState()
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to delete folder")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete folder")
            }
        }
    }

    fun addBookmark(request: BookmarkCreateRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val client = ApiClient.getCurrentClient()
                val token = ApiClient.authToken ?: throw Exception("Not logged in")
                val result = client.createBookmark(token, request)
                if (result.isSuccess) {
                    onSuccess()
                    loadBookmarks(reset = true) // Refresh list
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to add bookmark")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add bookmark")
            }
        }
    }

    fun addFolder(name: String, color: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val client = ApiClient.getCurrentClient()
                val token = ApiClient.authToken ?: throw Exception("Not logged in")
                val request = FolderCreateRequest(name = name, color = color)
                val result = client.createFolder(token, request)
                if (result.isSuccess) {
                    onSuccess()
                    loadBookmarks(reset = true) // Refresh list including folders
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to add pod")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add pod")
            }
        }
    }

    fun editBookmark(id: String, request: BookmarkUpdateRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val client = ApiClient.getCurrentClient()
                val token = ApiClient.authToken ?: throw Exception("Not logged in")
                val result = client.updateBookmark(token, id, request)
                if (result.isSuccess) {
                    onSuccess()
                    loadBookmarks(reset = true) // Refresh list
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to update bookmark")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update bookmark")
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutComplete()
        }
    }
}
