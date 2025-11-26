package moe.okay.nuistnotification.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import moe.okay.nuistnotification.data.Notification
import moe.okay.nuistnotification.data.NotificationDao
import moe.okay.nuistnotification.data.NotificationEntity
import moe.okay.nuistnotification.network.RetrofitClient
import moe.okay.nuistnotification.parser.HtmlParser

class NotificationRepository(
    private val dao: NotificationDao
) {
    private val notificationService = RetrofitClient.nuistNotificationService

    fun observeAll(): Flow<List<Notification>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun refreshNotifications(): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            val page = notificationService.getFirstPage()
            val list = HtmlParser.parseNotifications(page)
            if (list.isNotEmpty()) {
                dao.insertAll(list.map { NotificationEntity.from(it) })
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
