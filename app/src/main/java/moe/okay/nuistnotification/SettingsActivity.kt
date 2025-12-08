package moe.okay.nuistnotification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moe.okay.nuistnotification.data.News
import moe.okay.nuistnotification.repository.NotificationRepository
import moe.okay.nuistnotification.repository.PreferenceRepository
import moe.okay.nuistnotification.ui.theme.NuistNotificationTheme
import java.util.concurrent.TimeUnit

class SettingsActivity : ComponentActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(context, SettingsActivity::class.java)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingScreen {
                finish()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SettingScreen(modifier: Modifier = Modifier, back: () -> Unit = {}) {
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationRepository = remember { NotificationRepository(context) }
    val preferenceRepository = remember { PreferenceRepository(context) }
    val frequency by notificationRepository.observeFrequencySeconds().collectAsState(
        initial = 60*60,
    )
    val notificationEnabled by notificationRepository.observeIsNotificationEnabled().collectAsState(
        initial = false,
    )
    val debugWorkerEnabled by preferenceRepository.observeDebugWorkerEnabled().collectAsState(
        initial = false,
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    val start = { seconds: Long ->
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            seconds, TimeUnit.SECONDS
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "NewsPollingWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    LaunchedEffect(notificationEnabled, hasNotificationPermission, frequency) {
        if (notificationEnabled && hasNotificationPermission) {
            start(frequency.toLong())
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("NewsPollingWork")
        }
    }



    NuistNotificationTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    title = { Text(text = "设置", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(
                            onClick = { back() }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "通知", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "开启通知")
                    Switch(
                        checked = notificationEnabled && hasNotificationPermission,
                        onCheckedChange = { b ->
                            if (Build.VERSION.SDK_INT >= 33 && b && !hasNotificationPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                notificationRepository.setNotificationEnabled(b)
                            }
                        }
                    )
                }
                if (notificationEnabled && hasNotificationPermission) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(text  = "更新频率")
                            Text(text = "抓取通知的时间间隔", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        var expanded by remember { mutableStateOf(false) }
                        val frequencies = listOf(15*60, 30*60, 60*60, 6*60*60, 12*60*60, 24*60*60)
                        val text = { freq: Int ->
                            when (freq) {
                                in 0 until 60 * 60 -> "${freq / 60} 分钟"
                                in 60 * 60 until 24 * 60 * 60 -> "${freq / 3600} 小时"
                                else -> "${freq / 86400} 天"
                            }
                        }
                        Box{
                            TextButton(
                                onClick = { expanded = !expanded }
                            ) {
                                Text(text(frequency))
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                frequencies.forEach { freq ->
                                    DropdownMenuItem(
                                        text = { Text(text(freq)) },
                                        onClick = {
                                            expanded = false
                                            CoroutineScope(Dispatchers.IO).launch {
                                                notificationRepository.setFrequencySeconds(freq)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                if(BuildConfig.DEBUG) {
                    Text(text = "调试", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "在轮询时发送通知")
                        Switch(
                            checked = debugWorkerEnabled,
                            onCheckedChange = { b ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    preferenceRepository.setDebugWorkerEnabled(b)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

class NotificationWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationRepository = NotificationRepository(context)
    private val preferenceRepository = PreferenceRepository(context)

    override suspend fun doWork(): Result {
        return try {
            val news = notificationRepository.refreshNotifications().getOrNull() ?: return Result.success()
            val lastSeenId = notificationRepository.getLastSeenId()
            Log.d("NotificationWorker", "Fetched ${news.size} news items, lastSeenId=$lastSeenId")
            val newNews = news.filter { n -> n.id > lastSeenId}.sortedBy { n -> n.id }
            if (!newNews.isEmpty()) {
                notificationRepository.setLastSeenId(newNews.maxBy { n-> n.id }.id)
                if (lastSeenId == 0) {
                    return Result.success()
                }
            }
            if (BuildConfig.DEBUG) {
                val debugEnabled = preferenceRepository.observeDebugWorkerEnabled().first()
                if (debugEnabled) {
                    val testNews = News(
                        id = (System.currentTimeMillis() / 1000).toInt(),
                        title = "测试通知",
                        summary = "后台任务正常工作，获取了${news.size}条，其中${newNews.size}条为新通知",
                        source = "",
                        date = "",
                        url = ""
                    )
                    showSystemNotification(context, testNews)
                }
            }
            newNews.forEach { n ->
                showSystemNotification(context, n)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

fun showSystemNotification(context: Context, news: News) {
    val channelId = "new_message_channel"
    val notificationId = news.id

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channel = NotificationChannel(
        channelId,
        "新消息通知",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "当有新数据时通知"
    }
    notificationManager.createNotificationChannel(channel)
    val intent = Intent(context, DetailActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("target", Json.encodeToString(news))
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(news.title)
        .setContentText(news.summary)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    notificationManager.notify(notificationId, notification)
}