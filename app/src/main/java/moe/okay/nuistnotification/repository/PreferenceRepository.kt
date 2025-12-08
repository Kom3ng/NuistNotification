package moe.okay.nuistnotification.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import moe.okay.nuistnotification.dataStore

class PreferenceRepository (
    val context: Context
) {
    private val debugWorkerEnabledKey = booleanPreferencesKey("is_debug_worker_enabled")
    suspend fun setDebugWorkerEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[debugWorkerEnabledKey] = enabled
        }
    }
    fun observeDebugWorkerEnabled() =
        context.dataStore.data.map { prefs ->
            prefs[debugWorkerEnabledKey] ?: false
        }
}