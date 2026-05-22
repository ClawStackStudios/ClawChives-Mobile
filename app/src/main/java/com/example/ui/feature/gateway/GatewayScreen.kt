package com.example.ui.feature.gateway

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.WarningBoxBg
import com.example.ui.theme.WarningText
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.ui.feature.gateway.GatewayViewModel
import com.example.ui.feature.gateway.GatewayUiState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun GatewayScreen(
    viewModel: GatewayViewModel,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val savedServerUrl by viewModel.savedServerUrl.collectAsStateWithLifecycle(initialValue = null)
  var isUploadMode by remember { mutableStateOf(true) }
  var protocol by remember { mutableStateOf("http") }
  var serverHost by remember { mutableStateOf("") }
  var serverPort by remember { mutableStateOf("") }
  var keyText by remember { mutableStateOf("") }
  var uploadedKey by remember { mutableStateOf<String?>(null) }
  var uploadedFileName by remember { mutableStateOf<String?>(null) }
  var isProtocolDropdownExpanded by remember { mutableStateOf(false) }
  var isHostFocused by remember { mutableStateOf(false) }
  var isPortFocused by remember { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current

  fun parseAndSetUrl(input: String) {
      var raw = input.trim()
      if (raw.isEmpty()) {
          serverHost = ""
          return
      }

      // Try to parse protocol
      if (raw.startsWith("https://", ignoreCase = true)) {
          protocol = "https"
          raw = raw.substring("https://".length)
      } else if (raw.startsWith("http://", ignoreCase = true)) {
          protocol = "http"
          raw = raw.substring("http://".length)
      }

      // Check for port in the remaining string
      if (raw.contains(":")) {
          val parts = raw.split(":")
          serverHost = parts[0]
          if (parts.size > 1) {
              val portAndPath = parts[1]
              val slashIndex = portAndPath.indexOf('/')
              if (slashIndex != -1) {
                  serverPort = portAndPath.substring(0, slashIndex).filter { it.isDigit() }
                  // Appending trailing path to the host
                  serverHost += portAndPath.substring(slashIndex)
              } else {
                  serverPort = portAndPath.filter { it.isDigit() }
              }
          }
      } else {
          serverHost = raw
      }
  }

  val context = LocalContext.current
  val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
  ) { uri ->
      uri?.let {
          try {
              val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
              if (text != null) {
                  var fileName = "identity.json"
                  context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                      val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                      if (nameIndex != -1 && cursor.moveToFirst()) {
                          fileName = cursor.getString(nameIndex)
                      }
                  }

                  val extractedKey = cleanAndExtractKey(text)
                  if (extractedKey.isNotEmpty()) {
                      uploadedKey = extractedKey
                      uploadedFileName = fileName
                  }
              }
          } catch (e: Exception) {
              e.printStackTrace()
          }
      }
  }

  // Pre-populate URL if savedServerUrl is available
  androidx.compose.runtime.LaunchedEffect(savedServerUrl) {
      savedServerUrl?.let { url ->
          if (serverHost.isEmpty() && url.isNotEmpty()) {
              parseAndSetUrl(url)
          }
      }
  }

  // Auto-navigate on successful session load or login
  androidx.compose.runtime.LaunchedEffect(uiState) {
      if (uiState is GatewayUiState.Success) {
          onLoginSuccess()
      }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(24.dp))

    // Logo (Simulated for now, would be a real image or vector)
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(RedAccent),
      contentAlignment = Alignment.Center
    ) {
      Text(text = "🦞", fontSize = 40.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Brand Name
    Text(
      text = buildAnnotatedString {
        withStyle(style = SpanStyle(color = CyanAccent)) { append("Claw") }
        withStyle(style = SpanStyle(color = RedAccent)) { append("Chives") }
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.5f), fontSize = 12.sp)) { append(" ©™") }
      },
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "Welcome Back",
      color = MaterialTheme.colorScheme.onBackground,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Login with your ClawChives©™ identity",
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
      fontSize = 16.sp
    )

    Spacer(modifier = Modifier.height(32.dp))

    val animatedPortWidth by animateDpAsState(
        targetValue = if (isPortFocused) 105.dp else 65.dp,
        label = "PortWidth"
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .zIndex(100f)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
              width = if (isHostFocused || isPortFocused) 1.5.dp else 1.dp,
              color = if (isPortFocused) RedAccent else if (isHostFocused) CyanAccent else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(12.dp)
            ),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 1. Seamless Protocol selector button (white text, cyan background)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxHeight()
              .background(CyanAccent)
              .clickable { isProtocolDropdownExpanded = !isProtocolDropdownExpanded }
              .padding(horizontal = 14.dp)
          ) {
            Text(
              text = if (protocol == "https") "https://" else "http://",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = if (isProtocolDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
              contentDescription = if (isProtocolDropdownExpanded) "Close protocol selection" else "Open protocol selection",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }

          // 2. Host input field
          Row(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier.weight(1f),
              contentAlignment = Alignment.CenterStart
            ) {
              if (serverHost.isEmpty()) {
                Text(
                  text = "my-chives.app",
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                  fontSize = 14.sp
                )
              }
              BasicTextField(
                value = serverHost,
                onValueChange = { parseAndSetUrl(it) },
                textStyle = TextStyle(
                  color = MaterialTheme.colorScheme.onBackground,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(if (isHostFocused) CyanAccent else MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                  onNext = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .onFocusChanged { isHostFocused = it.isFocused }
              )
            }

            // Contextual host save button
            androidx.compose.animation.AnimatedVisibility(
              visible = isHostFocused && serverHost.isNotEmpty(),
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically(),
              modifier = Modifier.padding(start = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color(0xFF4CAF50))
                  .clickable { focusManager.clearFocus() },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Done",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          // 3. Sliding/Highlight Divider
          Box(
            modifier = Modifier
              .width(1.dp)
              .height(24.dp)
              .background(if (isPortFocused) RedAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
          )

          // 4. Port section with slide animation and red focus highlight background
          Box(
            modifier = Modifier
              .width(animatedPortWidth)
              .height(56.dp)
              .background(if (isPortFocused) RedAccent.copy(alpha = 0.12f) else Color.Transparent)
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
              ) {
                if (serverPort.isEmpty()) {
                  Text(
                    text = "Port",
                    color = if (isPortFocused) RedAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                  )
                }
                BasicTextField(
                  value = serverPort,
                  onValueChange = { input -> serverPort = input.filter { it.isDigit() } },
                  textStyle = TextStyle(
                    color = if (isPortFocused) RedAccent else MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  cursorBrush = androidx.compose.ui.graphics.SolidColor(if (isPortFocused) RedAccent else MaterialTheme.colorScheme.onBackground),
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                  ),
                  keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isPortFocused = it.isFocused }
                )
              }

              // Contextual port save button
              androidx.compose.animation.AnimatedVisibility(
                visible = isPortFocused,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.padding(start = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4CAF50))
                    .clickable { focusManager.clearFocus() },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 5. Custom sliding dropdown menu for protocol selection
      androidx.compose.animation.AnimatedVisibility(
        visible = isProtocolDropdownExpanded,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        modifier = Modifier
          .zIndex(110f)
          .padding(start = 0.dp, top = 60.dp)
      ) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
          modifier = Modifier.width(110.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
          Column {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  protocol = "http"
                  isProtocolDropdownExpanded = false
                }
                .background(if (protocol == "http") CyanAccent.copy(alpha = 0.12f) else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "http://",
                color = if (protocol == "http") CyanAccent else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (protocol == "http") FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
              )
            }
            HorizontalDivider(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.padding(horizontal = 4.dp)
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  protocol = "https"
                  isProtocolDropdownExpanded = false
                }
                .background(if (protocol == "https") CyanAccent.copy(alpha = 0.12f) else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "https://",
                color = if (protocol == "https") CyanAccent else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (protocol == "https") FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
              )
            }
          }
        }
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))

    // Toggle Buttons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.2f))
        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
          .background(if (isUploadMode) CyanAccent else Color.Transparent)
          .clickable { isUploadMode = true }
          .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "Upload File", color = if (isUploadMode) Color.White else MaterialTheme.colorScheme.onBackground)
      }
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
          .background(if (!isUploadMode) CyanAccent else Color.Transparent)
          .clickable { isUploadMode = false }
          .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "Paste ClawKey©™", color = if (!isUploadMode) Color.White else MaterialTheme.colorScheme.onBackground)
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (isUploadMode) {
      UploadFileView(
        uploadedFileName = uploadedFileName,
        onTap = { launcher.launch("application/json") },
        onClear = {
          uploadedKey = null
          uploadedFileName = null
        }
      )
    } else {
      PasteKeyView(keyText, { keyText = it })
    }

    Spacer(modifier = Modifier.weight(1f))

    if (uiState is GatewayUiState.Error) {
      Text(
          text = (uiState as GatewayUiState.Error).message,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(bottom = 16.dp)
      )
    }

    val isFormValid = if (isUploadMode) {
        uploadedKey != null
    } else {
        keyText.isNotBlank()
    }

    Button(
      onClick = {
        val cleanHost = serverHost.trim()
          .removePrefix("https://")
          .removePrefix("http://")
          .removePrefix("HTTPS://")
          .removePrefix("HTTP://")
        val finalUrl = buildString {
          append(if (protocol == "https") "https://" else "http://")
          append(cleanHost)
          if (serverPort.isNotBlank()) {
            append(":")
            append(serverPort.trim())
          }
        }
        val targetKey = cleanAndExtractKey(if (isUploadMode) uploadedKey.orEmpty() else keyText)
        viewModel.login(finalUrl, targetKey)
      },
      enabled = uiState !is GatewayUiState.Loading && isFormValid,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
    ) {
      if (uiState is GatewayUiState.Loading) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
      } else {
        Text("Login with Identity", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
fun UploadFileView(
    uploadedFileName: String?,
    onTap: () -> Unit,
    onClear: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    // Dropzone
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .border(
            width = 1.dp,
            color = if (uploadedFileName != null) CyanAccent else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        )
        .clip(RoundedCornerShape(12.dp))
        .background(if (uploadedFileName != null) CyanAccent.copy(alpha = 0.05f) else Color.Transparent)
        .clickable { onTap() },
      contentAlignment = Alignment.Center
    ) {
      if (uploadedFileName != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
          Text(text = "🦞 Identity Loaded Successfully!", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "File: $uploadedFileName", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
             text = "Tap to change file",
             color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
             fontSize = 11.sp
          )
        }
      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Tap to upload your identity file",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
          )
          Text(
            text = ".json files only",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 14.sp
          )
        }
      }
    }

    if (uploadedFileName != null) {
      Spacer(modifier = Modifier.height(12.dp))
      Button(
         onClick = onClear,
         modifier = Modifier.fillMaxWidth(),
         colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
         shape = RoundedCornerShape(8.dp)
      ) {
         Text("Remove File", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Warning
    WarningBox()
  }
}

@Composable
fun PasteKeyView(keyText: String, onKeyTextChanged: (String) -> Unit) {
  var passwordVisible by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = keyText,
      onValueChange = onKeyTextChanged,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Paste your hu- or lb- key here...") },
      shape = RoundedCornerShape(12.dp),
      visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
      trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(
            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = if (passwordVisible) "Hide password" else "Show password",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyanAccent,
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
      )
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    WarningBox()
  }
}

@Composable
fun WarningBox() {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = WarningBoxBg),
    border = BorderStroke(1.dp, WarningBoxBg.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
      Icon(
        imageVector = Icons.Filled.Lock,
        contentDescription = "Warning",
        tint = WarningText,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text("Can't find your identity file?", color = WarningText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "Your identity file is the only way to access your account. If you've lost it, you'll need to create a new account.",
          color = WarningText.copy(alpha = 0.8f),
          fontSize = 14.sp,
          lineHeight = 20.sp
        )
      }
    }
  }
}

fun cleanAndExtractKey(rawInput: String): String {
    val trimmed = rawInput.trim()
    if (trimmed.isEmpty()) return ""
    
    // Check if it matches a JSON "token" field pattern
    val tokenJsonMatch = """"token"\s*:\s*"([^"]+)"""".toRegex().find(trimmed)
    if (tokenJsonMatch != null) {
        val extracted = tokenJsonMatch.groupValues[1].trim()
        if (extracted.isNotEmpty()) return extracted
    }
    
    // Look for any key starting with hu- or lb- followed by alphanumeric/dashes/underscores
    val keyPatternMatch = """(hu-|lb-)[a-zA-Z0-9_-]+""".toRegex().find(trimmed)
    if (keyPatternMatch != null) {
        return keyPatternMatch.value.trim()
    }
    
    // Strip trailing/leading extra quotes if present
    return trimmed.removeSurrounding("\"").removeSurrounding("'").trim()
}
