/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultRendererCapabilitiesList
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.RendererCapabilitiesList
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status.STAGE_LOADED_TO_POSITION_MS
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultAllocator
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Helper class for the Media3's [DefaultPreloadManager]. The main difference between this class and [DefaultPreloadManager] is the addition of the
 * [PlayerPool] argument. It allows the dynamic creation of a fixed number of [PillarboxExoPlayer] instances.
 *
 * This class provides the same methods as [DefaultPreloadManager] plus [getPlayer] and [getCurrentlyPlayingPlayer] to get an instance of a
 * [PillarboxExoPlayer].
 *
 * @param context The current [Context].
 * @param targetPreloadStatusControl The [TargetPreloadStatusControl] to decide when to preload an item and for how long.
 * @param mediaSourceFactory The [MediaSource.Factory] to create each [MediaSource].
 * @param trackSelector The [TrackSelector] for this preload manager.
 * @param bandwidthMeter The [BandwidthMeter] for this preload manager.
 * @param rendererCapabilitiesListFactory The [RendererCapabilitiesList.Factory] for this preload manager.
 * @param loadControl The [LoadControl] for this preload manager.
 * @param playbackThread The [Thread] on which the players run.
 * @param playersCount The maximum number of [PillarboxExoPlayer] to create.
 * @param playerFactory Called when a new [PillarboxExoPlayer] instance is necessary (up to `playersCount` times). The provided `Looper` **must**
 * be passed to [PillarboxExoPlayer]'s constructor.
 *
 * @see DefaultPreloadManager
 */
class PillarboxPreloadManager(
    context: Context,
    targetPreloadStatusControl: TargetPreloadStatusControl<Int>? = null,
    mediaSourceFactory: MediaSource.Factory = PillarboxMediaSourceFactory(context),
    trackSelector: TrackSelector = PillarboxTrackSelector(context).apply {
        init({}, PillarboxBandwidthMeter(context))
    },
    bandwidthMeter: BandwidthMeter = PillarboxBandwidthMeter(context),
    rendererCapabilitiesListFactory: RendererCapabilitiesList.Factory = DefaultRendererCapabilitiesList.Factory(
        PillarboxRenderersFactory(context)
    ),
    private val loadControl: LoadControl = PillarboxLoadControl(
        bufferDurations = PillarboxLoadControl.BufferDurations(
            minBufferDuration = 5.seconds,
            maxBufferDuration = 20.seconds,
            bufferForPlayback = 500.milliseconds,
        ),
        allocator = DefaultAllocator(false, C.DEFAULT_BUFFER_SEGMENT_SIZE),
    ),
    private val playbackThread: HandlerThread = HandlerThread("PillarboxPreloadManager:Playback", Process.THREAD_PRIORITY_AUDIO),
    playersCount: Int = 3,
    playerFactory: (playbackLooper: Looper) -> PillarboxExoPlayer = { playbackLooper ->
        PillarboxExoPlayer(
            context = context,
            loadControl = loadControl,
            playbackLooper = playbackLooper,
        )
    },
) {
    private val playerPool = PlayerPool(
        playersCount = playersCount,
        playerFactory = { playerFactory(playbackThread.looper) },
    )

    // We use a lazy creation so the playbackThread can be started first
    private val preloadManager by lazy {
        DefaultPreloadManager(
            targetPreloadStatusControl ?: DefaultTargetPreloadStatusControl(),
            mediaSourceFactory,
            trackSelector,
            bandwidthMeter,
            rendererCapabilitiesListFactory,
            loadControl.allocator,
            playbackThread.looper,
        )
    }

    /**
     * The index of the currently playing media item.
     *
     * @see DefaultPreloadManager.setCurrentPlayingIndex
     */
    var currentPlayingIndex: Int = C.INDEX_UNSET
        set(value) {
            preloadManager.setCurrentPlayingIndex(value)
            field = value
        }

    /**
     * Get the count of [MediaSource] currently managed by this preload manager.
     *
     * @see DefaultPreloadManager.getSourceCount
     */
    val sourceCount: Int
        get() = preloadManager.sourceCount

    init {
        playbackThread.start()
    }

    /**
     * Add a [MediaItem] with its [rankingData] to the preload manager.
     *
     * @param mediaItem The [MediaItem] to add.
     * @param rankingData The ranking data that is associated with the [mediaItem].
     * @see DefaultPreloadManager.add
     */
    fun add(mediaItem: MediaItem, rankingData: Int) {
        preloadManager.add(mediaItem, rankingData)
    }

    /**
     * Add a [MediaSource] with its [rankingData] to the preload manager.
     *
     * @param mediaSource The [MediaSource] to add.
     * @param rankingData The ranking data that is associated with the [mediaSource].
     * @see DefaultPreloadManager.add
     */
    fun add(mediaSource: MediaSource, rankingData: Int) {
        preloadManager.add(mediaSource, rankingData)
    }

    /**
     * Returns the [MediaSource] for the given [MediaItem].
     *
     * @param mediaItem The [MediaItem].
     * @return The source for the give [mediaItem] if it is managed by the preload manager, `null` otherwise.
     * @see DefaultPreloadManager.getMediaSource
     */
    fun getMediaSource(mediaItem: MediaItem): MediaSource? {
        return preloadManager.getMediaSource(mediaItem)
    }

    /**
     * Invalidate the current preload manager.
     *
     * @see DefaultPreloadManager.invalidate
     */
    fun invalidate() {
        preloadManager.invalidate()
    }

    /**
     * Release the preload manager and the underlying [PlayerPool].
     * The preload manager must not be used after calling this method.
     *
     * @see DefaultPreloadManager.release
     */
    fun release() {
        playerPool.release()
        preloadManager.release()
        playbackThread.quit()
    }

    /**
     * Remove a [MediaItem] from the preload manager.
     *
     * @param mediaItem The [MediaItem] to remove.
     * @return `true` if the preload manager is holding a [MediaSource] of the given [MediaItem] and it has been removed, `false` otherwise.
     * @see DefaultPreloadManager.remove
     */
    fun remove(mediaItem: MediaItem): Boolean {
        return preloadManager.remove(mediaItem)
    }

    /**
     * Remove a [MediaSource] from the preload manager.
     *
     * @param mediaSource The [MediaSource] to remove.
     * @return `true` if the preload manager is holding the given [MediaSource] and it has been removed, `false` otherwise.
     * @see DefaultPreloadManager.remove
     */
    fun remove(mediaSource: MediaSource): Boolean {
        return preloadManager.remove(mediaSource)
    }

    /**
     * Reset the preload manager. All sources that the preload manager is holding will be released.
     *
     * @see DefaultPreloadManager.reset
     */
    fun reset() {
        preloadManager.reset()
    }

    /**
     * Get a [PillarboxExoPlayer] for the given [index]. If the desired player has not been created yet, [PlayerPool.playerFactory] will be called.
     *
     * @param index The index of the [PillarboxExoPlayer] to retrieve.
     * @return The desired [PillarboxExoPlayer], or `null` if [index] is negative.
     */
    fun getPlayer(index: Int): PillarboxExoPlayer? {
        return playerPool.getPlayerAtPosition(index)
    }

    /**
     * Get the currently playing [PillarboxExoPlayer].
     *
     * @return The currently playing [PillarboxExoPlayer], or `null` if there is no active player.
     */
    fun getCurrentlyPlayingPlayer(): PillarboxExoPlayer? {
        return getPlayer(currentPlayingIndex)
    }

    /**
     * Default implementation of [TargetPreloadStatusControl] that will preload the first second of the `n ± 1` item, and the first half-second of
     * the `n ± 2,3` item, where `n` is the index of the current item.
     */
    @Suppress("MagicNumber")
    inner class DefaultTargetPreloadStatusControl : TargetPreloadStatusControl<Int> {
        override fun getTargetPreloadStatus(rankingData: Int): TargetPreloadStatusControl.PreloadStatus? {
            val offset = abs(rankingData - currentPlayingIndex)

            return when (offset) {
                1 -> Status(STAGE_LOADED_TO_POSITION_MS, 1.seconds.inWholeMicroseconds)
                2, 3 -> Status(STAGE_LOADED_TO_POSITION_MS, 500.milliseconds.inWholeMicroseconds)
                else -> null
            }
        }
    }
}
