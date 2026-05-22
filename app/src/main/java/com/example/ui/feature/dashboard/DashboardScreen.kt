package com.example.ui.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import com.example.data.remote.Bookmark
import com.example.data.remote.BookmarkCreateRequest
import com.example.data.remote.BookmarkUpdateRequest
import com.example.data.remote.Folder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.WarningText
import com.example.ui.theme.MutedText
import kotlinx.coroutines.launch

import com.example.ui.feature.addbookmark.AddBookmarkDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    sharedUrlFlow: StateFlow<String?>,
    onSharedUrlConsumed: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddPodDialog by remember { mutableStateOf(false) }
    var bookmarkToEdit by remember { mutableStateOf<Bookmark?>(null) }
    val context = LocalContext.current
    val sharedUrl by sharedUrlFlow.collectAsStateWithLifecycle(initialValue = null)

    val activeFilter = (uiState as? DashboardState.Success)?.selectedFilter ?: "all"
    val activeFolderId = (uiState as? DashboardState.Success)?.selectedFolderId
    val activeSearch = (uiState as? DashboardState.Success)?.searchQuery ?: ""
    val folders = (uiState as? DashboardState.Success)?.folders ?: emptyList()
    val stats = (uiState as? DashboardState.Success)?.stats
    val tagsCount = (uiState as? DashboardState.Success)?.tagsCount ?: 0

    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null) {
            bookmarkToEdit = null
            showAddDialog = true
        }
    }

    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState is DashboardState.Success) {
            viewModel.loadBookmarks(reset = false)
        }
    }

    if (showAddDialog) {
        AddBookmarkDialog(
            initialUrl = sharedUrl,
            bookmarkToEdit = bookmarkToEdit,
            availablePods = folders,
            onDismiss = {
                showAddDialog = false
                bookmarkToEdit = null
                if (sharedUrl != null) onSharedUrlConsumed()
            },
            onSubmitAdd = { request ->
                viewModel.addBookmark(
                    request = request,
                    onSuccess = {
                        showAddDialog = false
                        bookmarkToEdit = null
                        if (sharedUrl != null) onSharedUrlConsumed()
                        Toast.makeText(context, "Pinchmark added!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            },
            onSubmitEdit = { id, request ->
                viewModel.editBookmark(
                    id = id,
                    request = request,
                    onSuccess = {
                        showAddDialog = false
                        bookmarkToEdit = null
                        Toast.makeText(context, "Pinchmark updated!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showAddPodDialog) {
        AddPodDialog(
            onDismiss = { showAddPodDialog = false },
            onSubmit = { name, color ->
                viewModel.addFolder(
                    name = name,
                    color = color,
                    onSuccess = {
                        showAddPodDialog = false
                        Toast.makeText(context, "Pod '$name' created!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                // 1. Logo Brand Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = CyanAccent)) { append("Claw") }
                                withStyle(style = SpanStyle(color = RedAccent)) { append("Chives") }
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "🦞",
                            fontSize = 22.sp
                        )
                    }
                    Text(
                        text = "ClawStack Studios©™",
                        color = MutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // 2. Search Box
                OutlinedTextField(
                    value = activeSearch,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search pinchmarks...", fontSize = 13.sp, color = MutedText) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = MutedText) },
                    trailingIcon = if (activeSearch.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )

                // 3. Navigation items and folder list (scrolling area)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // All Pinchmarks
                    DrawerNavItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null, tint = if (activeFilter == "all" && activeFolderId == null) CyanAccent else MutedText) },
                        label = "All Pinchmarks",
                        badgeValue = stats?.total,
                        selected = activeFilter == "all" && activeFolderId == null,
                        activeBgColor = Color(0x1F0EA5E9),
                        activeTextColor = CyanAccent,
                        onClick = {
                            viewModel.setFilter("all")
                            scope.launch { drawerState.close() }
                        }
                    )

                    // Starred
                    DrawerNavItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = null, tint = if (activeFilter == "starred" && activeFolderId == null) WarningText else MutedText) },
                        label = "Starred",
                        badgeValue = stats?.starred,
                        selected = activeFilter == "starred" && activeFolderId == null,
                        activeBgColor = Color(0x1FF59E0B),
                        activeTextColor = WarningText,
                        onClick = {
                            viewModel.setFilter("starred")
                            scope.launch { drawerState.close() }
                        }
                    )

                    // Tags
                    DrawerNavItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = if (activeFilter == "tags" && activeFolderId == null) Color(0xFF0284C7) else MutedText) },
                        label = "Tags",
                        badgeValue = tagsCount,
                        selected = activeFilter == "tags" && activeFolderId == null,
                        activeBgColor = Color(0x1F0284C7),
                        activeTextColor = Color(0xFF0284C7),
                        onClick = {
                            viewModel.setFilter("tags")
                            scope.launch { drawerState.close() }
                        }
                    )

                    // Archived
                    DrawerNavItem(
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = if (activeFilter == "archived" && activeFolderId == null) RedAccent else MutedText) },
                        label = "Archived",
                        badgeValue = stats?.archived,
                        selected = activeFilter == "archived" && activeFolderId == null,
                        activeBgColor = Color(0x1FEF4444),
                        activeTextColor = RedAccent,
                        onClick = {
                            viewModel.setFilter("archived")
                            scope.launch { drawerState.close() }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )

                    // Pods (Folders) Header with Add Pod Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 24.dp, start = 24.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PODS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                .clickable {
                                    showAddPodDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Pod",
                                tint = MutedText,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Folders List
                    if (folders.isEmpty()) {
                        Text(
                            text = "No folders created",
                            fontSize = 13.sp,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    } else {
                        folders.forEach { folder ->
                            val isFolderSelected = activeFolderId == folder.id
                            val folderColor = remember(folder.color) {
                                try {
                                    Color(android.graphics.Color.parseColor(folder.color ?: "#06b6d4"))
                                } catch (e: Exception) {
                                    CyanAccent
                                }
                            }
                            DrawerNavItem(
                                icon = {
                                    Canvas(modifier = Modifier.size(10.dp)) {
                                        drawCircle(color = folderColor)
                                    }
                                },
                                label = folder.name,
                                badgeValue = null,
                                selected = isFolderSelected,
                                activeBgColor = Color(0x1F0EA5E9),
                                activeTextColor = CyanAccent,
                                onClick = {
                                    viewModel.selectFolder(folder.id)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // 4. Action Footer Settings/System
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Settings
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = CyanAccent) },
                        label = { Text("Settings", fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Database Stats
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = "Database Stats", tint = WarningText) },
                        label = { Text("Database Stats", fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = {
                            viewModel.loadBookmarks(reset = true)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    // Claw Out
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Claw Out", tint = RedAccent) },
                        label = { Text("Claw Out", fontWeight = FontWeight.Bold, color = RedAccent) },
                        selected = false,
                        onClick = {
                            viewModel.logout(onLogout)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                val titleText = when {
                    activeFolderId != null -> {
                        folders.find { it.id == activeFolderId }?.name ?: "Pod View"
                    }
                    activeFilter == "starred" -> "Starred Pinchmarks"
                    activeFilter == "archived" -> "Archived Pinchmarks"
                    activeFilter == "tags" -> "Tagged Pinchmarks"
                    else -> "All Pinchmarks"
                }
                TopAppBar(
                    title = { Text(titleText, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        IconButton(onClick = { 
                            bookmarkToEdit = null
                            showAddDialog = true 
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Bookmark", tint = CyanAccent)
                        }
                    }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CyanAccent
                    )
                }
                is DashboardState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error loading pool", color = RedAccent)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadBookmarks() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is DashboardState.Success -> {
                    if (state.bookmarks.isEmpty()) {
                        Text(
                            text = "Your reef is empty.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.bookmarks,
                                key = { it.id }
                            ) { bookmark ->
                                PinchmarkCard(
                                    bookmark = bookmark,
                                    onLongPress = {
                                        bookmarkToEdit = bookmark
                                        showAddDialog = true
                                    },
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.url))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }

                            if (state.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = CyanAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun PinchmarkCard(
    bookmark: Bookmark,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bookmark.title ?: bookmark.url,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bookmark.url,
                        color = CyanAccent,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (bookmark.pinned || bookmark.starred || bookmark.archived) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (bookmark.pinned) {
                            Text("📌", fontSize = 14.sp)
                        }
                        if (bookmark.starred) {
                            Text("⭐", fontSize = 14.sp)
                        }
                        if (bookmark.archived) {
                            Text("📦", fontSize = 14.sp)
                        }
                    }
                }
            }

            if (!bookmark.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bookmark.description,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (bookmark.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                @OptIn(ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    bookmark.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerNavItem(
    icon: @Composable () -> Unit,
    label: String,
    badgeValue: Int?,
    selected: Boolean,
    activeBgColor: Color,
    activeTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) activeBgColor else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = if (selected) activeTextColor else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (badgeValue != null) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) activeTextColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeValue.toString(),
                    color = if (selected) activeTextColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

