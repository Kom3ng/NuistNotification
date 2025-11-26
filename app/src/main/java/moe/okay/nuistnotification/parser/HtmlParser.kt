package moe.okay.nuistnotification.parser

import android.net.Uri
import android.util.Log
import moe.okay.nuistnotification.data.Notification
import org.jsoup.nodes.Document

object HtmlParser {
    fun parseNotifications(html: Document): List<Notification> = html.getElementsByClass("news")
            .map { e ->
                val node1 = e.selectFirst(".news_title") ?: return@map null
                val category = node1.selectFirst(".wjj [title]")?.attr("title") ?: return@map null
                val tmp = node1.selectFirst(".btt a") ?: return@map null
                val url = tmp.attr("href")
                val title = tmp.attr("title")
                val publisher = e.selectFirst(".news_org a")?.text() ?: return@map null
                val date = e.selectFirst(".news_date span")?.text() ?: return@map null
                val uri = Uri.parse(url)
                Notification(
                    treeId = uri.getQueryParameter("wbtreeid")?.toIntOrNull() ?: return@map null,
                    id = uri.getQueryParameter("wbnewsid")?.toIntOrNull() ?: return@map null,
                    title = title,
                    date = date,
                    url = url,
                    category = Notification.Category.entries.find { c -> c.displayName == category } ?: return@map null,
                    publisher = publisher
                )
            }
            .filterNotNull()
}