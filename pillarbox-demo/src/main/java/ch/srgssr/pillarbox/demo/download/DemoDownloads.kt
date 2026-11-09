/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.download

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadHelper
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.service.DemoDownloadService
import ch.srgssr.pillarbox.demo.shared.source.BlockedTimeRangeAssetLoader
import ch.srgssr.pillarbox.demo.shared.source.CustomAssetLoader
import ch.srgssr.pillarbox.download.PillarboxDownloadManager
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp

/**
 * Provides the application wide [PillarboxDownloadManager].
 */
object DemoDownloads {
    private const val MAX_VIDEO_WIDTH = 960
    private const val MAX_VIDEO_HEIGHT = 540

    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var downloadManager: PillarboxDownloadManager? = null

    /**
     * Returns the application wide [PillarboxDownloadManager], creating it if needed. Must be called from the main thread.
     *
     * @param context The [Context].
     */
    fun getDownloadManager(context: Context): PillarboxDownloadManager {
        return downloadManager ?: synchronized(this) {
            downloadManager ?: createDownloadManager(context.applicationContext).also { downloadManager = it }
        }
    }

    private fun createDownloadManager(context: Context): PillarboxDownloadManager {
        return PillarboxDownloadManager.Builder(context, DemoDownloadService::class.java)
            // Same asset loaders as the player, so that the media are downloaded in the same conditions as when they are streamed
            .addAssetLoader(CustomAssetLoader(context))
            .addAssetLoader(BlockedTimeRangeAssetLoader(context))
            .addAssetLoader(SRGAssetLoader(context))
            // Download a quality suited to phones, to save storage and data
            .setTrackSelectionParameters(
                DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS.buildUpon()
                    .setMaxVideoSize(MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT)
                    .build()
            )
            // SRG SSR streams may require an Akamai token
            .setDataSourceFactory(
                AkamaiTokenDataSource.Factory(
                    defaultDataSourceFactory = DefaultDataSource.Factory(context, OkHttpDataSource.Factory(PillarboxOkHttp())),
                )
            )
            .build()
    }
}
