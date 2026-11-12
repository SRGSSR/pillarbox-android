/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadProgress
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.UrlAssetLoader
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.offline.DownloadedMediaProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Downloads media to play them offline, using Media3's [DownloadManager].
 *
 * A media is first loaded by the configured [AssetLoader]s, to resolve the stream to download. A player configured with this manager as its
 * [DownloadedMediaProvider] then plays the downloaded media from the download cache.
 *
 * ```kotlin
 * val downloadManager = PillarboxDownloadManager.Builder(context, MyDownloadService::class.java)
 *     .addAssetLoader(SRGAssetLoader(context))
 *     .build()
 * val player = PillarboxExoPlayer(context) {
 *     downloadedMediaProvider(downloadManager)
 * }
 * ```
 *
 * Only one instance should be created per [downloadDirectory] in the application, as Media3's [SimpleCache] doesn't allow multiple instances
 * for the same directory. Creating it as a singleton is recommended.
 *
 * DRM protected content and live streams can't be downloaded.
 */
@Suppress("TooManyFunctions")
class PillarboxDownloadManager private constructor(builder: Builder) : DownloadedMediaProvider {
    private val context = builder.context
    private val downloadServiceClass = builder.downloadServiceClass
    private val assetLoaders = builder.assetLoaders.toList()
    private val defaultAssetLoader = UrlAssetLoader(DefaultMediaSourceFactory(context))
    private val dataSourceFactory = builder.dataSourceFactory
        ?: DefaultDataSource.Factory(context, OkHttpDataSource.Factory(builder.okHttpClient ?: PillarboxOkHttp()))
    private val trackSelectionParameters = builder.trackSelectionParameters
    private val databaseProvider = StandaloneDatabaseProvider(context)
    private val downloadExecutor = Executors.newFixedThreadPool(DOWNLOAD_THREAD_COUNT)

    /**
     * The directory in which the downloads are stored.
     */
    val downloadDirectory: File = builder.downloadDirectory
        ?: File(context.getExternalFilesDir(null) ?: context.filesDir, DEFAULT_DOWNLOAD_DIRECTORY)

    override val cache: Cache = SimpleCache(File(downloadDirectory, MEDIA_DIRECTORY), NoOpCacheEvictor(), databaseProvider)

    /**
     * The underlying Media3 [DownloadManager]. It must be returned by your [DownloadService.getDownloadManager] implementation.
     */
    val media3DownloadManager = DownloadManager(
        context,
        databaseProvider,
        cache,
        dataSourceFactory,
        downloadExecutor,
    ).apply {
        maxParallelDownloads = builder.maxParallelDownloads
        requirements = builder.requirements
    }

    /**
     * All the downloads, updated when a download changes and periodically while a download is in progress.
     *
     * The downloads are sorted by [start time][Download.startTimeMs], newest first. Pausing, resuming or completing a download doesn't change
     * its position, but restarting it may move it to the top.
     *
     * Each collector reads the downloads on its own. Share this flow, for example with `stateIn` in a `ViewModel`, rather than collecting it
     * several times.
     */
    val downloads: Flow<List<Download>> = channelFlow {
        val changes = Channel<Unit>(Channel.CONFLATED)
        val listener = object : DownloadManager.Listener {
            override fun onInitialized(downloadManager: DownloadManager) {
                changes.trySend(Unit)
            }

            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                changes.trySend(Unit)
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                changes.trySend(Unit)
            }
        }

        media3DownloadManager.addListener(listener)
        try {
            while (true) {
                val downloads = readDownloads()
                send(downloads)

                if (downloads.any { it.state == Download.STATE_DOWNLOADING }) {
                    withTimeoutOrNull(PROGRESS_UPDATE_INTERVAL_MS.milliseconds) { changes.receive() }
                } else {
                    changes.receive()
                }
            }
        } finally {
            media3DownloadManager.removeListener(listener)
        }
    }.flowOn(Dispatchers.Main)

    /**
     * Returns the identifier used for the download of [mediaItem]: its [MediaItem.mediaId] if set, its URI otherwise.
     *
     * @param mediaItem The [MediaItem].
     */
    fun getDownloadId(mediaItem: MediaItem): String {
        return checkNotNull(getDownloadIdOrNull(mediaItem)) { "MediaItem has neither a media id nor a URI" }
    }

    /**
     * Returns the [Download] for [mediaItem], or `null` if it has not been downloaded. This method accesses the database, avoid calling it
     * from the main thread.
     *
     * @param mediaItem The [MediaItem].
     */
    fun getDownload(mediaItem: MediaItem): Download? {
        val id = getDownloadIdOrNull(mediaItem) ?: return null
        return media3DownloadManager.downloadIndex.getDownload(id)
    }

    override fun getDownloadRequest(mediaItem: MediaItem): DownloadRequest? {
        return getDownload(mediaItem)
            ?.takeIf { it.state == Download.STATE_COMPLETED }
            ?.request
    }

    override fun getOfflineTrackersData(downloadRequest: DownloadRequest): ByteArray? {
        return DownloadData.decode(downloadRequest.data)?.trackersData
    }

    /**
     * Downloads [mediaItem].
     *
     * The media is first loaded with the configured [AssetLoader]s, to get the stream to download. Then the tracks to download are selected,
     * and the download is added to the [DownloadService]. The title of the media and [customData] are stored with the download, and can be
     * read with [Download.title] and [Download.customData]. The trackers data encoded by [AssetLoader.encodeOfflineTrackersData] is also
     * stored, so that the playback of the downloaded media is tracked.
     *
     * If [mediaItem] is already downloaded, or is being downloaded, the download is updated instead, as Media3 does: it is queued again
     * (and resumed if it was paused), the data already downloaded is kept, and the new tracks are added to the ones already selected.
     *
     * @param mediaItem The [MediaItem] to download.
     * @param trackSelectionParameters The [TrackSelectionParameters] used to select the tracks to download. By default, the ones set with
     * [Builder.setTrackSelectionParameters].
     * @param customData Application specific data to store with the download, for example the artwork URL of the media, or `null`.
     * @throws DownloadNotSupportedException If the media is DRM protected or is a live stream.
     * @throws IOException If the media can't be loaded.
     */
    suspend fun download(
        mediaItem: MediaItem,
        trackSelectionParameters: TrackSelectionParameters = this.trackSelectionParameters,
        customData: ByteArray? = null,
    ) {
        val id = getDownloadId(mediaItem)
        val assetLoader = assetLoaders.firstOrNull { it.canLoadAsset(mediaItem) } ?: defaultAssetLoader
        // Asset loaders may block, for example while reading a local file
        val asset = withContext(Dispatchers.IO) { assetLoader.loadAsset(mediaItem) }
        val streamConfiguration = checkNotNull(asset.mediaSource.mediaItem.localConfiguration)
        if (streamConfiguration.drmConfiguration != null) {
            throw DownloadNotSupportedException("DRM protected content can't be downloaded")
        }

        val title = (asset.mediaMetadata.title ?: mediaItem.mediaMetadata.title)?.toString()
        // Stored with the download, so that the playback of the downloaded media can be tracked offline.
        val trackersData = assetLoader.encodeOfflineTrackersData(asset.trackersData)
        val streamMediaItem = MediaItem.Builder()
            .setUri(streamConfiguration.uri)
            .setMimeType(streamConfiguration.mimeType)
            .build()

        val downloadRequest = withContext(Dispatchers.Main) {
            val downloadHelper = DownloadHelper.Factory()
                .setDataSourceFactory(dataSourceFactory)
                .setRenderersFactory(DefaultRenderersFactory(context))
                .setTrackSelectionParameters(trackSelectionParameters.withoutUnsupportedTrackTypes())
                .create(streamMediaItem)
            try {
                downloadHelper.prepareAndAwait()
                downloadHelper.getDownloadRequest(id, DownloadData(title, customData, trackersData).encode())
            } finally {
                downloadHelper.release()
            }
        }

        DownloadService.sendAddDownload(context, downloadServiceClass, downloadRequest, false)
    }

    /**
     * Pauses the download with the given [id].
     *
     * @param id The download identifier.
     */
    fun pause(id: String) {
        DownloadService.sendSetStopReason(context, downloadServiceClass, id, STOP_REASON_PAUSED, false)
    }

    /**
     * Resumes the download with the given [id], previously paused with [pause].
     *
     * @param id The download identifier.
     */
    fun resume(id: String) {
        DownloadService.sendSetStopReason(context, downloadServiceClass, id, Download.STOP_REASON_NONE, false)
    }

    /**
     * Queues the download with the given [id] again, for example after it failed. The data already downloaded is kept, only the missing data
     * is downloaded. A paused download is resumed, and a download in progress continues.
     *
     * @param id The download identifier.
     */
    suspend fun restart(id: String) {
        val download = withContext(Dispatchers.IO) { media3DownloadManager.downloadIndex.getDownload(id) } ?: return

        // Adding a download that already exists queues it again
        DownloadService.sendAddDownload(context, downloadServiceClass, download.request, false)
    }

    /**
     * Cancels the download with the given [id] if it is in progress, and removes the downloaded data.
     *
     * @param id The download identifier.
     */
    fun remove(id: String) {
        DownloadService.sendRemoveDownload(context, downloadServiceClass, id, false)
    }

    /**
     * Releases the resources of this manager: the downloads are stopped, and the download cache is closed. This manager, and any player
     * using it as its [DownloadedMediaProvider], must not be used afterward.
     *
     * An application wide manager usually doesn't need to be released.
     */
    fun release() {
        media3DownloadManager.release()
        downloadExecutor.shutdown()
        cache.release()
        databaseProvider.close()
    }

    private fun getDownloadIdOrNull(mediaItem: MediaItem): String? {
        return mediaItem.mediaId.takeIf { it != MediaItem.DEFAULT_MEDIA_ID }
            ?: mediaItem.localConfiguration?.uri?.toString()
    }

    private suspend fun readDownloads(): List<Download> {
        val storedDownloads = withContext(Dispatchers.IO) {
            media3DownloadManager.downloadIndex.getDownloads().use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.download)
                    }
                }
            }
        }
        // The current downloads contain the live progress of the downloads in progress. Their progress is updated in place, so a snapshot
        // is taken: each emission then contains new instances, which are seen as changed by StateFlow and Compose.
        val currentDownloads = media3DownloadManager.currentDownloads.associateBy { it.request.id }

        return storedDownloads
            .map { currentDownloads[it.request.id]?.snapshot() ?: it }
            .sortedByDescending { it.startTimeMs }
    }

    /**
     * Builder of a [PillarboxDownloadManager].
     *
     * @param context The [Context].
     * @param downloadServiceClass The [DownloadService] declared in your application manifest, that uses the built
     * [PillarboxDownloadManager]. See [PillarboxDownloadService].
     */
    class Builder(context: Context, downloadServiceClass: Class<out DownloadService>) {
        internal val context: Context = context.applicationContext
        internal val downloadServiceClass: Class<out DownloadService> = downloadServiceClass
        internal val assetLoaders = mutableListOf<AssetLoader>()
        internal var downloadDirectory: File? = null
            private set
        internal var okHttpClient: OkHttpClient? = null
            private set
        internal var dataSourceFactory: DataSource.Factory? = null
            private set
        internal var maxParallelDownloads: Int = DownloadManager.DEFAULT_MAX_PARALLEL_DOWNLOADS
            private set
        internal var requirements: Requirements = DownloadManager.DEFAULT_REQUIREMENTS
            private set
        internal var trackSelectionParameters: TrackSelectionParameters = DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS
            private set

        /**
         * Adds an [AssetLoader] used to resolve the stream of the media to download. They are checked in the order they are
         * added; URLs are handled by default. Use the same [AssetLoader]s as your player.
         *
         * @param assetLoader The [AssetLoader] to add.
         * @return this builder for convenience.
         */
        fun addAssetLoader(assetLoader: AssetLoader): Builder {
            assetLoaders.add(assetLoader)
            return this
        }

        /**
         * Sets the directory in which the downloads are stored. By default, a `pillarbox-downloads` directory in the application specific
         * external storage, or internal storage if not available.
         *
         * @param downloadDirectory The download directory.
         * @return this builder for convenience.
         */
        fun setDownloadDirectory(downloadDirectory: File): Builder {
            this.downloadDirectory = downloadDirectory
            return this
        }

        /**
         * Sets the [OkHttpClient] used to download the media. Ignored if a [DataSource.Factory] is set with [setDataSourceFactory].
         *
         * @param okHttpClient The [OkHttpClient].
         * @return this builder for convenience.
         */
        fun setHttpClient(okHttpClient: OkHttpClient): Builder {
            this.okHttpClient = okHttpClient
            return this
        }

        /**
         * Sets the [DataSource.Factory] used to download the media. By default, a [DefaultDataSource.Factory] using the [OkHttpClient] set
         * with [setHttpClient].
         *
         * @param dataSourceFactory The [DataSource.Factory].
         * @return this builder for convenience.
         */
        fun setDataSourceFactory(dataSourceFactory: DataSource.Factory): Builder {
            this.dataSourceFactory = dataSourceFactory
            return this
        }

        /**
         * Sets the maximum number of downloads running in parallel.
         *
         * @param maxParallelDownloads The maximum number of parallel downloads. Must be greater than 0.
         * @return this builder for convenience.
         */
        fun setMaxParallelDownloads(maxParallelDownloads: Int): Builder {
            require(maxParallelDownloads > 0) { "maxParallelDownloads must be greater than 0" }
            this.maxParallelDownloads = maxParallelDownloads
            return this
        }

        /**
         * Sets the [Requirements] to meet for the downloads to progress, for example [Requirements.NETWORK_UNMETERED] to download over
         * Wi-Fi only.
         *
         * @param requirements The [Requirements].
         * @return this builder for convenience.
         */
        fun setRequirements(requirements: Requirements): Builder {
            this.requirements = requirements
            return this
        }

        /**
         * Sets the [TrackSelectionParameters] used to select the tracks to download.
         *
         * @param trackSelectionParameters The [TrackSelectionParameters].
         * @return this builder for convenience.
         */
        fun setTrackSelectionParameters(trackSelectionParameters: TrackSelectionParameters): Builder {
            this.trackSelectionParameters = trackSelectionParameters
            return this
        }

        /**
         * Builds the [PillarboxDownloadManager]. Only one instance should be created per download directory, see [PillarboxDownloadManager].
         *
         * @return A new [PillarboxDownloadManager].
         */
        fun build(): PillarboxDownloadManager {
            return PillarboxDownloadManager(this)
        }
    }

    companion object {
        private const val DEFAULT_DOWNLOAD_DIRECTORY = "pillarbox-downloads"
        private const val DOWNLOAD_THREAD_COUNT = 6
        private const val MEDIA_DIRECTORY = "media"
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L

        /**
         * [Download.stopReason] of a download paused with [pause].
         */
        const val STOP_REASON_PAUSED = 1

        /**
         * Returns a copy of this [Download] whose progress doesn't change anymore.
         */
        private fun Download.snapshot(): Download {
            val progress = DownloadProgress().also {
                it.bytesDownloaded = bytesDownloaded
                it.percentDownloaded = percentDownloaded
            }

            return Download(request, state, startTimeMs, updateTimeMs, contentLength, stopReason, failureReason, progress)
        }

        private suspend fun DownloadHelper.prepareAndAwait() {
            suspendCancellableCoroutine { continuation ->
                prepare(object : DownloadHelper.Callback {
                    override fun onPrepared(helper: DownloadHelper, tracksInfoAvailable: Boolean) {
                        continuation.resume(Unit)
                    }

                    override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                        val exception = if (e is DownloadHelper.LiveContentUnsupportedException) {
                            DownloadNotSupportedException("Live streams can't be downloaded", e)
                        } else {
                            e
                        }
                        continuation.resumeWithException(exception)
                    }
                })
            }
        }

        /**
         * Disables the track types that can't be part of a download request. For example, HLS exposes the ID3 metadata of audio renditions as
         * a separate track, which makes [DownloadHelper.getDownloadRequest] fail if it is selected.
         */
        private fun TrackSelectionParameters.withoutUnsupportedTrackTypes(): TrackSelectionParameters {
            return buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_METADATA, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_IMAGE, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_CAMERA_MOTION, true)
                .build()
        }
    }
}

/**
 * Exception thrown when a media can't be downloaded, for example because it is DRM protected or a live stream.
 *
 * @param message The detail message.
 * @param cause The cause.
 */
class DownloadNotSupportedException(message: String, cause: Throwable? = null) : IOException(message, cause)
