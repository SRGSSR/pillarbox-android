/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.Util
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.demo.R
import java.util.concurrent.Executors

/**
 * Pillarbox download service
 * doc : https://developer.android.com/media/media3/exoplayer/downloading-media
 * @constructor Create empty Pillarbox download service
 */
class PillarboxDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    NOTIFICATION_CHANNEL_ID,
    R.string.download_notification_Channel_name,
    0
) {

    override fun onCreate() {
        super.onCreate()
        Log.d("DOWNLOAD", "create service")
    }

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = getDownloadManager(this)
        val downloadNotificationHelper = getDownloadNotificationHelper(this)
        downloadManager.addListener(TerminalStateNotificationHelper(this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1))
        return downloadManager
    }

    override fun getScheduler(): Scheduler? {
        return WorkManagerScheduler(this, WORK_NAME)
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        return getDownloadNotificationHelper(this).buildProgressNotification(
            this,
            R.drawable.baseline_download_24,
            null,
            null,
            downloads,
            notMetRequirements,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DOWNLOAD", "DownloadService::onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    inner class TerminalStateNotificationHelper(
        private val context: Context,
        private val notificationHelper: DownloadNotificationHelper,
        private var nextNotificationId: Int
    ) :
        DownloadManager.Listener {

        override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
            Log.d("DOWNLOAD", "onDownloadChanged ${download.request.uri} / ${download.request.id}")
            val notification = when (download.state) {
                Download.STATE_COMPLETED -> {
                    notificationHelper.buildDownloadCompletedNotification(
                        context,
                        R.drawable.baseline_download_24,
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }

                Download.STATE_FAILED -> {
                    notificationHelper.buildDownloadFailedNotification(
                        context,
                        R.drawable.baseline_download_done_24,
                        null,
                        Util.fromUtf8Bytes(download.request.data)
                    )
                }

                else -> {
                    return
                }
            }
            NotificationUtil.setNotification(context, nextNotificationId++, notification)
        }
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        private const val WORK_NAME = "DemoDownloadManager"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val THREADS_COUNT = 6
        private const val NOTIFICATION_CHANNEL_ID = "Downloads channel"

        private var dataBaseProvider: DatabaseProvider? = null
        private var cache: Cache? = null
        private var downloadManager: DownloadManager? = null
        private var downloadNotificationHelper: DownloadNotificationHelper? = null

        private fun getDatabaseProvider(context: Context): DatabaseProvider {
            if (dataBaseProvider == null) {
                dataBaseProvider = StandaloneDatabaseProvider(context)
            }
            return dataBaseProvider!!
        }

        fun getDownloadNotificationHelper(context: Context): DownloadNotificationHelper {
            if (downloadNotificationHelper == null) {
                downloadNotificationHelper = DownloadNotificationHelper(context, NOTIFICATION_CHANNEL_ID)
            }
            return downloadNotificationHelper!!
        }

        fun getCache(context: Context): Cache {
            if (cache == null) {
                val downloadContentDirectory = context.getExternalFilesDir(null) ?: context.filesDir
                Log.d("DOWNLOAD", "Download dir $downloadContentDirectory")
                cache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider(context))
            }
            return cache!!
        }

        fun getDownloadManager(context: Context): DownloadManager {
            if (downloadManager == null) {
                downloadManager = DownloadManager(
                    context,
                    getDatabaseProvider(context = context),
                    getCache(context = context),
                    AkamaiTokenDataSource.Factory(),
                    Executors.newFixedThreadPool(THREADS_COUNT),
                )
            }
            return downloadManager!!
        }
    }
}
