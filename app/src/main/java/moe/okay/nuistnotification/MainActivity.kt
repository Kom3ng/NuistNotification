package moe.okay.nuistnotification

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import moe.okay.nuistnotification.ui.screens.NotificationScreen
import moe.okay.nuistnotification.ui.screens.NotificationScreenViewModel
import moe.okay.nuistnotification.ui.theme.NuistNotificationTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun App(modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    NuistNotificationTheme {
        CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
            val searchBarScrollBehavior: SearchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
            val lazyListState = rememberLazyListState()
            Scaffold(
                modifier = modifier
                    .fillMaxSize()
                    .nestedScroll(searchBarScrollBehavior.nestedScrollConnection),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                topBar = {
                    val textFieldState = rememberTextFieldState()
                    val searchBarState = rememberSearchBarState()
                    AppBarWithSearch(
                        modifier = Modifier.padding(4.dp),
                        colors = SearchBarDefaults.appBarWithSearchColors(
                            appBarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            scrolledSearchBarContainerColor = MaterialTheme.colorScheme.surface,
                            searchBarColors = SearchBarDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(Icons.Default.FilterList, "filter")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    SettingsActivity.start(context = context)
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = "settings",
                                )
                            }
                        },
                        state = searchBarState,
                        inputField = {
                            SearchBarDefaults.InputField(
                                searchBarState = searchBarState,
                                textFieldState = textFieldState,
                                onSearch = {},
                                placeholder = { Text("搜索通知") },
                            )
                        },
                        scrollBehavior = searchBarScrollBehavior
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                floatingActionButton = {
                    ScrollToTopFab(lazyListState)
                }
            ) { innerPadding ->
                val notificationScreenViewModel: NotificationScreenViewModel = viewModel()
                LaunchedEffect(Unit) {
                    notificationScreenViewModel.initializeFirstPage()
                }
                Box(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    NotificationScreen(viewModel = notificationScreenViewModel, lazyListState = lazyListState)
                }
            }
        }
    }
}

@Composable
fun ScrollToTopFab(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    AnimatedVisibility(
        visible = showButton,
        enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = "Scroll to top"
            )
        }
    }
}
