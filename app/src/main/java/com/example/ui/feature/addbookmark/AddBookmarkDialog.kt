package com.example.ui.feature.addbookmark

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import com.example.data.remote.Bookmark
import com.example.data.remote.BookmarkCreateRequest
import com.example.data.remote.BookmarkUpdateRequest
import com.example.data.remote.Folder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightText
import com.example.ui.theme.MutedText
import kotlin.math.abs

@Composable
fun AddBookmarkDialog(
    initialUrl: String? = null,
    bookmarkToEdit: Bookmark? = null,
    availablePods: List<Folder> = emptyList(),
    onDismiss: () -> Unit,
    onSubmitAdd: ((BookmarkCreateRequest) -> Unit)? = null,
    onSubmitEdit: ((String, BookmarkUpdateRequest) -> Unit)? = null
) {
    var url by remember { mutableStateOf(bookmarkToEdit?.url ?: initialUrl ?: "") }
    var urlError by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(bookmarkToEdit?.title ?: "") }
    var description by remember { mutableStateOf(bookmarkToEdit?.description ?: "") }
    
    var pod by remember { mutableStateOf(availablePods.find { it.id == bookmarkToEdit?.folderId }?.name ?: "No Pod") }
    var selectedPodId by remember { mutableStateOf(bookmarkToEdit?.folderId) }
    var showPodDropdown by remember { mutableStateOf(false) }
    
    var tagInput by remember { mutableStateOf("") }
    val addedTags = remember { mutableStateListOf<String>().apply { bookmarkToEdit?.tags?.let { addAll(it) } } }
    var showManageTags by remember { mutableStateOf(false) }

    var isJina by remember { mutableStateOf(bookmarkToEdit?.jinaUrl != null) }
    var isStar by remember { mutableStateOf(bookmarkToEdit?.starred ?: false) }
    var isArch by remember { mutableStateOf(bookmarkToEdit?.archived ?: false) }
    // API doesn't have `pinned` natively unless color/pinned was a thing, but UI shows PIN.
    var isPin by remember { mutableStateOf(false) }

    LaunchedEffect(url) {
        if ((url.startsWith("http://") || url.startsWith("https://")) && bookmarkToEdit == null) {
            kotlinx.coroutines.delay(1000)
            val metadata = com.example.util.UrlParser.fetchMetadata(url)
            if (title.isBlank() && !metadata.first.isNullOrBlank()) {
                title = metadata.first ?: ""
            }
            if (description.isBlank() && !metadata.second.isNullOrBlank()) {
                description = metadata.second ?: ""
            }
        }
    }

    if (showManageTags) {
        ManageTagsDialog(
            tags = addedTags,
            onRemove = { addedTags.remove(it) },
            onDismiss = { showManageTags = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, CyanAccent)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (bookmarkToEdit != null) "EDIT PINCHMARK" else "ADD PINCHMARK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (bookmarkToEdit != null) "MODIFY YOUR SAVED URL" else "PINCH A URL INTO YOUR COLLECTION",
                            color = MutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedText)
                    }
                }

                HorizontalDivider(color = CyanAccent.copy(alpha = 0.5f), thickness = 1.dp)

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomTextField(
                        label = "URL",
                        value = url,
                        onValueChange = { 
                            url = it
                            if (urlError) urlError = false
                        },
                        placeholder = "https://example.com",
                        isError = urlError
                    )

                    CustomTextField(
                        label = "TITLE",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Pinchmark title"
                    )

                    CustomTextField(
                        label = "DESCRIPTION",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add a description...",
                        singleLine = false,
                        minLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("POD", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                            Box {
                                OutlinedTextField(
                                    value = pod,
                                    onValueChange = {}, // Read only
                                    readOnly = true,
                                    trailingIcon = { Icon(Icons.Outlined.Folder, contentDescription = null, tint = MutedText) },
                                    modifier = Modifier.fillMaxWidth().clickable { showPodDropdown = true },
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    enabled = false // Disable to let Box intercept clicks
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { showPodDropdown = true })
                                
                                DropdownMenu(
                                    expanded = showPodDropdown,
                                    onDismissRequest = { showPodDropdown = false },
                                    modifier = Modifier.background(DarkSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("No Pod", color = LightText) },
                                        onClick = { 
                                            pod = "No Pod"
                                            selectedPodId = null
                                            showPodDropdown = false 
                                        }
                                    )
                                    availablePods.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.name, color = LightText) },
                                            onClick = { 
                                                pod = p.name
                                                selectedPodId = p.id
                                                showPodDropdown = false 
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("TAGS", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = tagInput,
                                    onValueChange = { tagInput = it },
                                    placeholder = { Text("Add tags...", color = MutedText) },
                                    modifier = Modifier.weight(1f),
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .clickable { 
                                            if (tagInput.isNotBlank() && !addedTags.contains(tagInput.trim())) {
                                                addedTags.add(tagInput.trim())
                                                tagInput = ""
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 24.sp)
                                }
                            }
                            
                            // Render Tags below
                            if (addedTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                if (addedTags.size <= 2) {
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        addedTags.forEach { tag ->
                                            TagPill(tag, onRemove = { addedTags.remove(tag) })
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { showManageTags = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_manage),
                                            contentDescription = null,
                                            tint = LightText,
                                            modifier = Modifier.size(14.dp).padding(end = 4.dp)
                                        )
                                        Text("TAGS (${addedTags.size})", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Checkboxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomCheckbox(label = "🦞 JINA", checked = isJina, onCheckedChange = { isJina = it }, labelColor = Color(0xFFF59E0B))
                        CustomCheckbox(label = "☆ STAR", checked = isStar, onCheckedChange = { isStar = it })
                        CustomCheckbox(label = "ARCH", checked = isArch, onCheckedChange = { isArch = it })
                        CustomCheckbox(label = "PIN", checked = isPin, onCheckedChange = { isPin = it })
                    }
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (url.isBlank()) {
                                urlError = true
                                return@Button
                            }
                            
                            val finalUrl = if (!url.trim().startsWith("http://") && !url.trim().startsWith("https://")) {
                                "https://${url.trim()}"
                            } else {
                                url.trim()
                            }
                            
                            if (bookmarkToEdit == null) {
                                val request = BookmarkCreateRequest(
                                    url = finalUrl,
                                    title = title.takeIf { it.isNotBlank() } ?: finalUrl,
                                    description = description.takeIf { it.isNotBlank() },
                                    tags = addedTags.toList(),
                                    folderId = selectedPodId,
                                    starred = isStar,
                                    archived = isArch,
                                    jinaUrl = if (isJina) "https://r.jina.ai/$finalUrl" else null
                                )
                                onSubmitAdd?.invoke(request)
                            } else {
                                val request = BookmarkUpdateRequest(
                                    url = finalUrl,
                                    title = title.takeIf { it.isNotBlank() } ?: finalUrl,
                                    description = description.takeIf { it.isNotBlank() },
                                    tags = addedTags.toList(),
                                    folderId = selectedPodId,
                                    starred = isStar,
                                    archived = isArch,
                                    color = bookmarkToEdit.color,
                                    favicon = bookmarkToEdit.favicon,
                                    jinaUrl = if (isJina) "https://r.jina.ai/$finalUrl" else null
                                )
                                onSubmitEdit?.invoke(bookmarkToEdit.id, request)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("PINCH IT! \uD83E\uDD9E", fontWeight = FontWeight.Bold, color = DarkBackground) // 🦞
                    }
                }
            }
        }
    }
}

val TagColors = listOf(
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF59E0B), // Amber
    Color(0xFFEF4444)  // Lobster Red
)

fun getTagColor(tag: String): Color {
    val hash = abs(tag.hashCode())
    return TagColors[hash % TagColors.size]
}

@Composable
fun TagPill(tag: String, onRemove: () -> Unit) {
    val color = getTagColor(tag)
    Row(
        modifier = Modifier
            .background(DarkSurface, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(tag.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Default.Close, 
            contentDescription = "Remove tag",
            tint = color,
            modifier = Modifier
                .size(12.dp)
                .clickable { onRemove() }
        )
    }
}

@Composable
fun ManageTagsDialog(
    tags: List<String>,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, CyanAccent)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "MANAGE TAGS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(tags) { tag ->
                        val color = getTagColor(tag)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tag.uppercase(), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { onRemove(tag) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove tag", tint = color)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = DarkBackground
                    )
                ) {
                    Text("DONE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false
) {
    Column {
        Text(text = label, color = if (isError) MaterialTheme.colorScheme.error else MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MutedText) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            colors = customTextFieldColors(),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun customTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface,
    disabledContainerColor = DarkSurface,
    focusedIndicatorColor = CyanAccent.copy(alpha = 0.5f),
    unfocusedIndicatorColor = Color(0xFF334155),
    focusedTextColor = LightText,
    unfocusedTextColor = LightText
)

@Composable
fun CustomCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, labelColor: Color = MutedText) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(if (checked) CyanAccent else Color.Transparent, RoundedCornerShape(2.dp))
                .border(if (checked) 0.dp else 1.dp, if (checked) CyanAccent else Color.White, RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(Icons.Default.Check, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
