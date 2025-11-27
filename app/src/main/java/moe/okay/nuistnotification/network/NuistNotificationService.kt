package moe.okay.nuistnotification.network

import moe.okay.nuistnotification.data.NewsListXml
import moe.okay.nuistnotification.data.Stat
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.lang.reflect.Type

interface NuistNotificationService {

    @GET("/index/{index}.vsb.xml")
    suspend fun getPage(@Path("index") index: Int): NewsListXml
    @GET("/index/statxml.js")
    suspend fun getStat(): Stat
}

object RetrofitClient {

    private const val BASE_URL = "https://bulletin.nuist.edu.cn"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JsoupConverterFactory.create())
            .build()
    }

    val nuistNotificationService: NuistNotificationService by lazy {
        retrofit.create(NuistNotificationService::class.java)
    }
}

class JsoupConverterFactory : Converter.Factory() {

    companion object {
        fun create(): JsoupConverterFactory = JsoupConverterFactory()
        private val statRegex = Regex("""var\s+(\w+)\s*=\s*(\d+)\s*;""")
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return when(type) {
            Document::class.java -> Converter<ResponseBody, Document> { body ->
                    body.use { b -> Jsoup.parse(b.string()) }
                }
            NewsListXml::class.java ->  Converter<ResponseBody, NewsListXml> { body ->
                body.use { b ->
                    XML.decodeFromString(NewsListXml.serializer(), b.string())
                }
            }
            Stat::class.java -> Converter<ResponseBody, Stat> { body ->
                body.use { b ->
                    val html = b.string()
                    val variables = statRegex.findAll(html)
                        .associate { it.groupValues[1] to it.groupValues[2].toInt() }

                    Stat(
                        rowCount = variables["rowCount"] ?: 0,
                        pageCount = variables["pageCount"] ?: 0,
                        xmlCount = variables["xmlCount"] ?: 0,
                        xmlPages = variables["xmlPages"] ?: 0,
                        totalPages = variables["totalPages"] ?: 0
                    )
                }
            }
            else -> null
        }
    }
}