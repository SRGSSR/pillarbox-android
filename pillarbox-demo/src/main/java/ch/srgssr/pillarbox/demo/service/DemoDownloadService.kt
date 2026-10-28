/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.Notification
import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import ch.srgssr.pillarbox.demo.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.concurrent.Executor

/**
 * [DownloadService] used by the download showcase.
 */
class DemoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.download_notification_channel_name,
    NOTIFICATION_TITLE_RES_ID
) {
    // Create a factory for reading the data from the network.
    private val dataSourceFactory = DefaultHttpDataSource.Factory()

    // Using Runnable::run will cause each download task to download data on its own thread.
    private val downloadExecutor = Executor(Runnable::run)

    override fun getDownloadManager(): DownloadManager {
        return DownloadManager(
            this,
            getDatabaseProvider(this),
            getDownloadCache(this),
            dataSourceFactory,
            downloadExecutor
        ).apply {
            maxParallelDownloads = MAX_PARALLEL_DOWNLOADS
            addListener(downloadListener)
        }
    }

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(downloads: MutableList<Download>, notMetRequirements: Int): Notification {
        return DownloadNotificationHelper(this, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .buildProgressNotification(
                this,
                android.R.drawable.stat_sys_download,
                null,
                null,
                downloads,
                notMetRequirements
            )
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "downloads"
        private const val NOTIFICATION_TITLE_RES_ID = 0
        private const val JOB_ID = 1
        private const val MAX_PARALLEL_DOWNLOADS = 3

        // These must be singletons: only one SimpleCache may use a given folder per process.
        private var databaseProvider: StandaloneDatabaseProvider? = null
        private var downloadCache: SimpleCache? = null

        private val _downloads = MutableStateFlow<Map<String, Download>>(emptyMap())

        /**
         * Latest state of every download, keyed by download ID (the media URI).
         */
        val downloads: StateFlow<Map<String, Download>> = _downloads.asStateFlow()

        private val downloadListener = object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                _downloads.update { it + (download.request.id to download) }
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                _downloads.update { it - download.request.id }
            }

            override fun onInitialized(downloadManager: DownloadManager) {
                // Load the downloads persisted by earlier sessions, including completed ones.
                val persisted = buildMap {
                    downloadManager.downloadIndex.getDownloads().use { cursor ->
                        while (cursor.moveToNext()) {
                            put(cursor.download.request.id, cursor.download)
                        }
                    }
                }
                _downloads.update { persisted + it }
            }
        }

        /**
         * Reads downloaded media from the cache, falling back to the network for anything missing.
         */
        fun getCacheDataSourceFactory(context: Context): DataSource.Factory {
            return CacheDataSource.Factory()
                .setCache(getDownloadCache(context))
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                .setCacheWriteDataSinkFactory(null) // Disable writing.
        }

        @Synchronized
        internal fun getDatabaseProvider(context: Context): StandaloneDatabaseProvider {
            return databaseProvider ?: StandaloneDatabaseProvider(context.applicationContext)
                .also { databaseProvider = it }
        }

        // A download cache should not evict media, so should use a NoOpCacheEvictor.
        @Synchronized
        internal fun getDownloadCache(context: Context): SimpleCache {
            return downloadCache ?: SimpleCache(
                File(context.applicationContext.filesDir, "downloads"),
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            ).also { downloadCache = it }
        }
    }
}
