package moe.okay.nuistnotification.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import moe.okay.nuistnotification.data.News
import moe.okay.nuistnotification.data.NotificationDao
import moe.okay.nuistnotification.data.NotificationEntity
import moe.okay.nuistnotification.network.RetrofitClient

class NotificationRepository(
    private val dao: NotificationDao
) {
    private val notificationService = RetrofitClient.nuistNotificationService

    fun observeAll(): Flow<List<News>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun refreshNotifications(): Result<List<News>> = withContext(Dispatchers.IO) {
        try {
            val stat = notificationService.getStat()
            val n = notificationService.getPage(stat.xmlPages)
            val news = n.data.map { it.toDomain() }
            dao.insertAll(news.map(NotificationEntity::from))
            Result.success(news)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
