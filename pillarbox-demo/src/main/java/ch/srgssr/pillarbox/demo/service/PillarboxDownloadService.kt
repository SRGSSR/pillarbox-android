/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.Notification
import android.content.Context
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
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
class PillarboxDownloadService : DownloadService(FOREGROUND_NOTIFICATION_ID) {
    private lateinit var downloadNotificationHelper: DownloadNotificationHelper
    private lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        super.onCreate()
        downloadNotificationHelper = DownloadNotificationHelper(this, CHANNEL_ID)
        downloadManager = getDownloadManager(this)
    }

    override fun getDownloadManager(): DownloadManager {
        return downloadManager
    }

    override fun getScheduler(): Scheduler? {
        return WorkManagerScheduler(this, WORK_NAME)
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        return downloadNotificationHelper.buildProgressNotification(
            this,
            R.drawable.baseline_download_24,
            null,
            null,
            downloads,
            Requirements.NETWORK
        )
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        private const val WORK_NAME = "DemoDownloadManager"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "DemoDownload"
        private const val THREADS_COUNT = 6

        private var dataBaseProvider: DatabaseProvider? = null
        private var cache: Cache? = null
        private var downloadManager: DownloadManager? = null

        private fun getDatabaseProvider(context: Context): DatabaseProvider {
            if (dataBaseProvider == null) {
                dataBaseProvider = StandaloneDatabaseProvider(context)
            }
            return dataBaseProvider!!
        }

        fun getCache(context: Context): Cache {
            if (cache == null) {
                val downloadContentDirectory = context.getExternalFilesDir(null) ?: context.filesDir
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
