package moe.okay.nuistnotification.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("data")
data class NewsListXml(
    @XmlElement(true) val data: List<NewsXml>,
)
@Serializable
@SerialName("news")
data class NewsXml(
    val id: Int,
    @XmlElement(true) @SerialName("showTitle") val title: String,
    @XmlElement(true) @SerialName("wbsourcename") val source: String = "其他",
    @XmlElement(true) @SerialName("showdidian") val location: String? = null,
    @XmlElement(true) @SerialName("showbaogaoren") val presenter: String? = null,
    @XmlElement(true) @SerialName("showbaogaoDate") val presenterDate: String? = null,
    @XmlElement(true) @SerialName("showzhuchi") val host: String? = null,
    @XmlElement(true) @SerialName("showDate") val date: String? = null,
    @XmlElement(true) @SerialName("picUrl") val imageUrl: String? = null,
    @XmlElement(true) @SerialName("url") val link: String,
    @XmlElement(true) @SerialName("showAbstract") val summary: String? = null
) {
    fun toDomain(): News {
        return News(
            id = id,
            title = title,
            source = source,
            date = date ?: "",
            url = link,
            summary = summary
        )
    }
}

@Serializable
data class News(
    val id: Int,
    val title: String,
    val source: String,
    val date: String,
    val url: String,
    val summary: String?,
)

data class Stat(
    val rowCount: Int,
    val pageCount:Int,
    val xmlCount:Int,
    val xmlPages:Int,
    val totalPages:Int,
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val source: String,
    val date: String,
    val url: String,
    val summary: String?,
) {
    fun toDomain(): News{
        return News(
            id = id,
            title = title,
            source = source,
            date = date,
            url = url,
            summary = summary
        )
    }
    companion object {
        fun from(news: News): NotificationEntity {
            return NotificationEntity(
                id = news.id,
                title = news.title,
                source = news.source,
                date = news.date,
                url = news.url,
                summary = news.summary
            )
        }
    }
}