package moe.okay.nuistnotification.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val treeId: Int,
    val id: Int,
    val title: String,
    val category: Category,
    val date: String,
    val url: String,
    val publisher: String,
) {
    @Serializable
    enum class Category(val displayName: String) {
        AcademicReports("学术报告"),
        BiddingInformation("招标信息"),
        MeetingNotices("会议通知"),
        PartyAndGovernmentAffairs("党政事务"),
        OrganizationAndPersonnel("组织人事"),
        ScientificResearch("科研信息"),
        AdmissionsAndEmployment("招生就业"),
        TeachingAndExams("教学考试"),
        InnovationAndEntrepreneurship("创新创业"),
        AcademicSeminars("学术研讨"),
        SpecialLectures("专题讲座"),
        CampusActivities("校园活动"),
        CollegeNews("学院动态"),
        Other("其他"),
    }
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    val treeId: Int,
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val url: String,
    val publisher: String,
) {
    companion object {
        fun from(notification: Notification): NotificationEntity {
            return NotificationEntity(
                treeId = notification.treeId,
                id = notification.id,
                title = notification.title,
                category = notification.category.displayName,
                date = notification.date,
                url = notification.url,
                publisher = notification.publisher,
            )
        }
    }

    fun toDomain(): Notification {
        val categoryEnum = Notification.Category.entries.find { it.displayName == category }
            ?: Notification.Category.Other
        return Notification(
            treeId = treeId,
            id = id,
            title = title,
            category = categoryEnum,
            date = date,
            url = url,
            publisher = publisher
        )
    }
}