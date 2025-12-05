package moe.okay.nuistnotification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewFeature
import kotlinx.serialization.json.Json
import moe.okay.nuistnotification.data.News
import moe.okay.nuistnotification.ui.theme.NuistNotificationTheme

class DetailActivity : ComponentActivity() {
    companion object {
        fun start(context: Context, target: News){
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("target", Json.encodeToString(target))
            }
            context.startActivity(intent)
        }
    }
    private val target: News by lazy {
        intent?.getStringExtra("target")?.let { Json.decodeFromString(News.serializer(), it) }!!
    }
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NuistNotificationTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            title = { Text(text = target.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { finish() }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        var isLoading by remember { mutableStateOf(true) }

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                CircularWavyProgressIndicator()
                            }
                        }

                        val backgroundColor = MaterialTheme.colorScheme.surface
                        val bgColorHex = String.format("#%02X%02X%02X",
                            (backgroundColor.red * 255).toInt(),
                            (backgroundColor.green * 255).toInt(),
                            (backgroundColor.blue * 255).toInt()
                        )

                        AndroidView(
                            modifier = Modifier.fillMaxSize().then(
                                if (isLoading) Modifier.alpha(0f) else Modifier.alpha(1f)
                            ),
                            factory = {
                                WebView(it).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    val css = """
                                        *, *::before, *::after {
                                            transition: none !important;
                                            animation: none !important;
                                        }
                                        body, html { overflow: hidden !important; }
                                        #container {
                                            position: fixed !important; top: 0 !important; left: 0 !important;
                                            width: 100vw !important; height: 100vh !important;
                                            z-index: 2147483647 !important;
                                            background-color: $bgColorHex !important;
                                            overflow-y: auto !important;
                                            box-sizing: border-box !important;
                                            padding: 0px 16px 16px 16px !important;
                                        }
                                        .ViewTit {
                                            display: none !important;
                                        }
                                        .ViewBox {
                                            padding-top: 16px !important;
                                        }
                                        .ViewPagination {
                                            display: none !important;
                                        }
                                    """.trimIndent()

                                    val js = "document.getElementsByTagName('head')[0].appendChild(document.createElement('style')).innerHTML = `$css`;"

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                                                view?.evaluateJavascript(js) {
                                                    postDelayed({
                                                        isLoading = false
                                                    }, 50)
                                                }
                                            }
                                        }
                                    }

                                    loadUrl("https://bulletin.nuist.edu.cn${target.url}")
                                }
                            },
                            update = {})
                    }
                }
            }
        }
    }
}