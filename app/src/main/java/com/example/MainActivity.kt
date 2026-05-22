package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.ClawChivesApplication
import com.example.ui.feature.dashboard.DashboardScreen
import com.example.ui.feature.dashboard.DashboardViewModel
import com.example.ui.feature.dashboard.DashboardViewModelFactory
import com.example.ui.feature.gateway.GatewayScreen
import com.example.ui.feature.gateway.GatewayViewModel
import com.example.ui.feature.gateway.GatewayViewModelFactory
import com.example.ui.theme.MyApplicationTheme

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
  private val sharedUrlState = MutableStateFlow<String?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent(intent)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        ClawChivesApp(
            sharedUrlFlow = sharedUrlState,
            onSharedUrlConsumed = { 
                sharedUrlState.value = null 
                intent.removeExtra(Intent.EXTRA_TEXT)
            }
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent) {
     if (Intent.ACTION_SEND == intent.action && "text/plain" == intent.type) {
         val url = intent.getStringExtra(Intent.EXTRA_TEXT)
         if (url != null) {
             sharedUrlState.value = url
         }
     }
  }
}

@Composable
fun ClawChivesApp(
    sharedUrlFlow: StateFlow<String?>,
    onSharedUrlConsumed: () -> Unit
) {
  val context = LocalContext.current
  val app = context.applicationContext as ClawChivesApplication
  val authRepository = app.authRepository
  val scope = androidx.compose.runtime.rememberCoroutineScope()

  val navController = rememberNavController()
  
  androidx.compose.runtime.LaunchedEffect(Unit) {
      com.example.data.remote.ApiClient.onUnauthorizedCallback = {
          scope.launch {
              val reauthSuccess = authRepository.attemptAutoReauth()
              if (!reauthSuccess) {
                  authRepository.logout()
                  val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
                  mainHandler.post {
                      navController.navigate("gateway") {
                          popUpTo(0) { inclusive = true }
                      }
                  }
              }
          }
      }
  }
  
  NavHost(
    navController = navController,
    startDestination = "gateway"
  ) {
    composable("gateway") {
      val gatewayViewModel: GatewayViewModel = viewModel(
        factory = GatewayViewModelFactory(authRepository)
      )

      Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
      ) { innerPadding ->
        GatewayScreen(
          viewModel = gatewayViewModel,
          modifier = Modifier.padding(innerPadding),
          onLoginSuccess = {
            navController.navigate("dashboard") {
              popUpTo("gateway") { inclusive = true }
            }
          }
        )
      }
    }
    composable("dashboard") {
      val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(authRepository)
      )

      DashboardScreen(
        viewModel = dashboardViewModel,
        sharedUrlFlow = sharedUrlFlow,
        onSharedUrlConsumed = onSharedUrlConsumed,
        onLogout = {
          navController.navigate("gateway") {
            popUpTo("dashboard") { inclusive = true }
          }
        }
      )
    }
  }
}

