package com.example.ui.feature.dashboard

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
        val isLoadingMore: Boolean = false,
        val isLastPage: Boolean = false
    ) : DashboardState
    data class Error(val message: String) : DashboardState
}

class DashboardViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private var cachedFolders = emptyList<Folder>()
    private val loadedBookmarks = mutableListOf<Bookmark>()
    private var currentPage = 1
    private var isLastPage = false
    private var isCurrentlyLoading = false
    private val pageSize = 50

    var selectedFilter = "all"
        private set
    var selectedFolderId: String? = null
        private set
    var searchQuery = ""
        private set

    private var cachedStats: BookmarkStats? = null
    private var cachedTagsCount = 0
    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadBookmarks(reset = true)
        viewModelScope.launch {
            authRepository.sessionRefreshed.collect {
                loadBookmarks(reset = true)
            }
        }
    }

    fun setFilter(filter: String) {
        if (selectedFilter == filter && selectedFolderId == null) return
        selectedFilter = filter
        selectedFolderId = null
        updateUIState()
    }

    fun selectFolder(folderId: String?) {
        if (selectedFolderId == folderId) return
        selectedFolderId = folderId
        selectedFilter = "all" // reset to all when inside folder
        updateUIState()
    }

    fun setSearchQuery(query: String) {
        if (searchQuery == query) return
        searchQuery = query
        updateUIState()
    }

    private fun updateUIState(forceLoadingMoreFalse: Boolean = false) {
        val currentState = (_uiState.value as? DashboardState.Success)
        
        val filtered = loadedBookmarks.filter { bookmark ->
            val matchesFolder = selectedFolderId == null || bookmark.folderId == selectedFolderId
            
            val matchesFilter = when (selectedFilter) {
                "starred" -> bookmark.starred == true
                "archived" -> bookmark.archived == true
                // if we add tags/pinned later, we can filter them here
                else -> bookmark.archived != true // Usually 'all' excludes archived unless 'archived' is selected
            }
            
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase()
                (bookmark.title?.lowercase()?.contains(q) == true) ||
                (bookmark.url.lowercase().contains(q)) ||
                (bookmark.description?.lowercase()?.contains(q) == true) ||
                bookmark.tags.any { it.lowercase().contains(q) }
            }
            
            matchesFolder && matchesFilter && matchesSearch
        }

        _uiState.value = DashboardState.Success(
            bookmarks = filtered,
            folders = cachedFolders,
            stats = cachedStats,
            tagsCount = cachedTagsCount,
            selectedFilter = selectedFilter,
            selectedFolderId = selectedFolderId,
            searchQuery = searchQuery,
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
                        cachedTagsCount = tagsResult.getOrThrow().size
                    }
                }

                val result = client.fetchBookmarks(
                    sessionToken = token,
                    starred = null,
                    archived = null,
                    folderId = null,
                    search = null,
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
