/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import android.content.Context
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import okhttp3.OkHttpClient
import java.io.File

/**
 * Creates a [PillarboxDownloadManager].
 *
 * @param context The [Context].
 * @param downloadServiceClass The [DownloadService] declared in your application manifest, that uses the returned [PillarboxDownloadManager].
 * See [PillarboxDownloadService].
 * @param block The block to configure the [PillarboxDownloadManager].
 * @return A new [PillarboxDownloadManager].
 */
@PillarboxDsl
fun PillarboxDownloadManager(
    context: Context,
    downloadServiceClass: Class<out DownloadService>,
    block: PillarboxDownloadManagerConfig.() -> Unit = {},
): PillarboxDownloadManager {
    return PillarboxDownloadManagerConfig(context.applicationContext, downloadServiceClass)
        .apply(block)
        .create()
}

/**
 * Configuration of a [PillarboxDownloadManager].
 */
@PillarboxDsl
class PillarboxDownloadManagerConfig internal constructor(
    private val context: Context,
    private val downloadServiceClass: Class<out DownloadService>,
) {
    private val assetLoaders = mutableListOf<AssetLoader>()
    private var downloadDirectory: File = File(context.getExternalFilesDir(null) ?: context.filesDir, DEFAULT_DOWNLOAD_DIRECTORY)
    private var okHttpClient: OkHttpClient = PillarboxOkHttp()
    private var dataSourceFactory: DataSource.Factory? = null
    private var maxParallelDownloads: Int = DownloadManager.DEFAULT_MAX_PARALLEL_DOWNLOADS
    private var requirements: Requirements = DownloadManager.DEFAULT_REQUIREMENTS
    private var trackSelectionParameters: TrackSelectionParameters = DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS

    /**
     * Adds an [AssetLoader] used to resolve the stream of the media to download. They are checked in the order they are
     * added; URLs are handled by default. Use the same [AssetLoader]s as your player.
     *
     * @param assetLoader The [AssetLoader] to add.
     */
    fun addAssetLoader(assetLoader: AssetLoader) {
        assetLoaders.add(assetLoader)
    }

    /**
     * Adds an [AssetLoader]. See [addAssetLoader].
     */
    operator fun AssetLoader.unaryPlus() {
        addAssetLoader(this)
    }

    /**
     * Sets the directory in which the downloads are stored. By default, a `pillarbox-downloads` directory in the application specific
     * external storage, or internal storage if not available.
     *
     * @param downloadDirectory The download directory.
     */
    fun downloadDirectory(downloadDirectory: File) {
        this.downloadDirectory = downloadDirectory
    }

    /**
     * Sets the [OkHttpClient] used to download the media.
     *
     * @param okHttpClient The [OkHttpClient].
     */
    fun httpClient(okHttpClient: OkHttpClient) {
        this.okHttpClient = okHttpClient
    }

    /**
     * Sets the [DataSource.Factory] used to download the media. By default, a [DefaultDataSource.Factory] using the [OkHttpClient] set with
     * [httpClient].
     *
     * @param dataSourceFactory The [DataSource.Factory].
     */
    fun dataSourceFactory(dataSourceFactory: DataSource.Factory) {
        this.dataSourceFactory = dataSourceFactory
    }

    /**
     * Sets the maximum number of downloads running in parallel.
     *
     * @param maxParallelDownloads The maximum number of parallel downloads. Must be greater than 0.
     */
    fun maxParallelDownloads(maxParallelDownloads: Int) {
        require(maxParallelDownloads > 0) { "maxParallelDownloads must be greater than 0" }
        this.maxParallelDownloads = maxParallelDownloads
    }

    /**
     * Sets the [Requirements] to meet for the downloads to progress, for example [Requirements.NETWORK_UNMETERED] to download over Wi-Fi only.
     *
     * @param requirements The [Requirements].
     */
    fun requirements(requirements: Requirements) {
        this.requirements = requirements
    }

    /**
     * Sets the [TrackSelectionParameters] used to select the tracks to download.
     *
     * @param trackSelectionParameters The [TrackSelectionParameters].
     */
    fun trackSelectionParameters(trackSelectionParameters: TrackSelectionParameters) {
        this.trackSelectionParameters = trackSelectionParameters
    }

    internal fun create(): PillarboxDownloadManager {
        return PillarboxDownloadManager(
            context = context,
            downloadServiceClass = downloadServiceClass,
            assetLoaders = assetLoaders.toList(),
            downloadDirectory = downloadDirectory,
            dataSourceFactory = dataSourceFactory ?: DefaultDataSource.Factory(context, OkHttpDataSource.Factory(okHttpClient)),
            trackSelectionParameters = trackSelectionParameters,
            maxParallelDownloads = maxParallelDownloads,
            requirements = requirements,
        )
    }

    private companion object {
        private const val DEFAULT_DOWNLOAD_DIRECTORY = "pillarbox-downloads"
    }
}
