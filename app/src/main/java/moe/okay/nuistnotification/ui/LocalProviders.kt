package moe.okay.nuistnotification

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

// Provide a global SnackbarHostState via CompositionLocal
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState?> { null }

