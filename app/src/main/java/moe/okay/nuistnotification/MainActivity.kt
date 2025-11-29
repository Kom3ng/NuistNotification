package moe.okay.nuistnotification

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import moe.okay.nuistnotification.ui.screens.NotificationScreen
import moe.okay.nuistnotification.ui.screens.NotificationScreenViewModel
import moe.okay.nuistnotification.ui.theme.NuistNotificationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    NuistNotificationTheme {
        CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppNavTopBar(navController = navController)
                },
                bottomBar = {
                    ShortNavigationBar {
                        ShortNavigationBarItem(
                            selected = true,
                            onClick = { /*TODO*/ },
                            label = { Text("通知") },
                            icon = { Icon(Icons.Default.Home, "home") },
                        )
                    }
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { innerPadding ->
                AppNavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                )
            }
        }
    }
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier, navController: NavHostController) {
    val notificationScreenViewModel: NotificationScreenViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Notifications,
        modifier = modifier,
    ) {
        composable<Notifications> {
            LaunchedEffect(Unit) {
                notificationScreenViewModel.initializeFirstPage()
            }
            NotificationScreen(notificationScreenViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavTopBar(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Notifications,
        modifier = modifier,
    ) {
        composable<Notifications> {
            TopAppBar(
                title = { Text("通知") },
            )
        }
    }
}

@Serializable
object Notifications