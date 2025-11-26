package moe.okay.nuistnotification.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.okay.nuistnotification.data.Notification
import moe.okay.nuistnotification.network.RetrofitClient
import moe.okay.nuistnotification.parser.HtmlParser

class NotificationRepository {
    private val notificationService = RetrofitClient.nuistNotificationService
    suspend fun fetchAndParseNotifications(): Result<List<Notification>> {
        return withContext(Dispatchers.IO) {
            try {
                val firstPage = notificationService.getFirstPage()
                Result.success(HtmlParser.parseNotifications(firstPage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}