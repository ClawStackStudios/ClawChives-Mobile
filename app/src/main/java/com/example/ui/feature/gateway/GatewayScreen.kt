package com.example.ui.feature.gateway

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.WarningBoxBg
import com.example.ui.theme.WarningText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
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
  val uiState by viewModel.uiState.collectAsState()
  var isUploadMode by remember { mutableStateOf(true) }
  var serverUrl by remember { mutableStateOf("") }
  var keyText by remember { mutableStateOf("") }

  val context = LocalContext.current
  val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
  ) { uri ->
      uri?.let {
          try {
              val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
              if (text != null) {
                  // Prioritize extractor for the "token" field from the identity JSON format
                  val tokenJsonMatch = """"token"\s*:\s*"([^"]+)"""".toRegex().find(text)
                  val extractedKey = tokenJsonMatch?.groupValues?.get(1)
                      ?: """(hu-|lb-)[a-zA-Z0-9_-]+""".toRegex().find(text)?.value
                      ?: text.trim()
                  
                  if (extractedKey.isNotEmpty()) {
                      keyText = extractedKey
                      isUploadMode = false
                  }
              }
          } catch (e: Exception) {
              e.printStackTrace()
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
    // Header Row
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = { /* TODO */ }) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onBackground
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Back to Home",
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp
      )
    }

    Spacer(modifier = Modifier.height(32.dp))

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

    OutlinedTextField(
      value = serverUrl,
      onValueChange = { serverUrl = it },
      modifier = Modifier.fillMaxWidth(),
      label = { Text("Server URL (e.g. https://my-chives.app)") },
      shape = RoundedCornerShape(12.dp)
    )
    
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
      UploadFileView(onTap = { launcher.launch("application/json") })
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

    Button(
      onClick = { viewModel.login(serverUrl, keyText) },
      enabled = uiState !is GatewayUiState.Loading,
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
fun UploadFileView(onTap: () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    // Dropzone
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .background(Color.Transparent)
        .clickable { onTap() },
      contentAlignment = Alignment.Center
    ) {
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
