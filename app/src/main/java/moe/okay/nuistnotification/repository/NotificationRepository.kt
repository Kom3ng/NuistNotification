package moe.okay.nuistnotification.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import moe.okay.nuistnotification.data.AppDatabase
import moe.okay.nuistnotification.data.News
import moe.okay.nuistnotification.data.NotificationDao
import moe.okay.nuistnotification.dataStore
import moe.okay.nuistnotification.network.RetrofitClient
import org.jsoup.nodes.Document


class NotificationRepository(
    private val context: Context,
) {
    private val dao: NotificationDao = AppDatabase.getInstance(context).notificationDao()
    private val notificationService = RetrofitClient.nuistNotificationService
    private val lastSeenIdKey = intPreferencesKey("last_seen_id")
    private val frequencyKey = intPreferencesKey("notification_check_frequency_seconds")
    private val isNotificationEnabledKey = booleanPreferencesKey("is_notification_enabled")

    fun observeAll(): Flow<List<News>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getLastSeenId(): Int {
        val prefs = context.dataStore.data.first()
        return prefs[lastSeenIdKey] ?: 0
    }
    suspend fun setLastSeenId(newId: Int) {
        context.dataStore.edit { prefs ->
            prefs[lastSeenIdKey] = newId
        }
    }
    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[isNotificationEnabledKey] = enabled
        }
    }
    suspend fun setFrequencySeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[frequencyKey] = seconds
        }
    }
    fun observeFrequencySeconds(): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[frequencyKey] ?: 3600
        }
    fun observeIsNotificationEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[isNotificationEnabledKey] ?: false
        }
    suspend fun refreshNotifications(): Result<List<News>> = withContext(Dispatchers.IO) {
        try {
            val stat = notificationService.getStat()
            val n = notificationService.getPage(stat.xmlPages)
            val news = n.data.map { it.toDomain() }
//            dao.insertAll(news.map(NotificationEntity::from))
            Result.success(news)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadMoreNotifications(page: Int): Result<List<News>> = withContext(Dispatchers.IO) {
        try {
            val s = notificationService.getStat()
            val n = notificationService.getPage(s.xmlPages - page + 1)
            val news = n.data.map { it.toDomain() }
//            dao.insertAll(news.map(NotificationEntity::from))
            Result.success(news)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHtml(url: String) : Result<Document> = withContext(Dispatchers.IO) {
        try {
            val doc = notificationService.getHtml(url)
            Result.success(doc)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error fetching HTML for URL: $url", e)
            Result.failure(e)
        }
    }
}
