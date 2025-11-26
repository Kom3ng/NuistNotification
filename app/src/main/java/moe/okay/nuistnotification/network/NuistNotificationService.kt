package moe.okay.nuistnotification.network

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.lang.reflect.Type

interface NuistNotificationService {
    @GET("/")
    suspend fun getFirstPage(): Document
    @GET("/index/{index}.htm")
    suspend fun getPage(@Path("index") index: Int): Document
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
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return if (type == Document::class.java) {
            Converter<ResponseBody, Document> { body ->
                body.use { b -> Jsoup.parse(b.string()) }
            }
        } else {
            null
        }
    }
}