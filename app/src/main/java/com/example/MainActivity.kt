package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AdminPhoneScreen
import com.example.ui.AppMode
import com.example.ui.AuthScreen
import com.example.ui.PublicTvPreviewScreen
import com.example.ui.SchoolViewModel
import com.example.ui.TvSignageScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val schoolViewModel: SchoolViewModel = viewModel()
    var isLocalAdminLoggedIn by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var showAuthScreen by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val isLoggedIn = (currentUser != null) || isLocalAdminLoggedIn

    if (!isLoggedIn) {
        if (showAuthScreen) {
            AuthScreen(
                onAuthSuccess = {
                    isLocalAdminLoggedIn = true
                    showAuthScreen = false
                },
                onCancel = {
                    showAuthScreen = false
                }
            )
        } else {
            // Live TV display preview directly on launch (like YouTube preview)
            PublicTvPreviewScreen(
                viewModel = schoolViewModel,
                onOpenLogin = {
                    showAuthScreen = true
                }
            )
        }
    } else {
        // Authenticated session: connect real-time Firestore ViewModel
        SchoolAppRoot(
            viewModel = schoolViewModel,
            onSignOut = {
                isLocalAdminLoggedIn = false
                showAuthScreen = false
                currentUser = null
            }
        )
    }
}

@Composable
fun SchoolAppRoot(
    viewModel: SchoolViewModel,
    onSignOut: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()

    val isLandscapeOrTv = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
            configuration.screenWidthDp >= 720

    val shouldShowTv = when (appMode) {
        AppMode.TV_SIGNAGE, AppMode.TV_PREVIEW -> true
        AppMode.ADMIN_CONTROLLER -> false
        AppMode.AUTO -> isLandscapeOrTv
    }

    if (shouldShowTv) {
        BackHandler {
            viewModel.setAppMode(AppMode.ADMIN_CONTROLLER)
        }
        TvSignageScreen(
            viewModel = viewModel,
            onBackToPhoneMode = {
                viewModel.setAppMode(AppMode.ADMIN_CONTROLLER)
            },
            isPreviewMode = !isLandscapeOrTv || appMode == AppMode.TV_PREVIEW
        )
    } else {
        AdminPhoneScreen(
            viewModel = viewModel,
            onOpenTvSignage = {
                viewModel.setAppMode(AppMode.TV_PREVIEW)
            },
            onSignOut = {
                viewModel.setAppMode(AppMode.AUTO)
                onSignOut()
            }
        )
    }
}
