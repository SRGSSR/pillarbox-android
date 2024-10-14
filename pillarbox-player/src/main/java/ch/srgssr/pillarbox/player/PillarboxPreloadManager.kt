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
import androidx.media3.exoplayer.RendererCapabilitiesList
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status.STAGE_LOADED_TO_POSITION_MS
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.upstream.Allocator
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultAllocator
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Helper class for the Media3's [DefaultPreloadManager].
 *
 * @param context The current [Context].
 * @param targetPreloadStatusControl The [TargetPreloadStatusControl] to decide when to preload an item and for how long.
 * @param mediaSourceFactory The [PillarboxMediaSourceFactory] to create each [MediaSource].
 * @param trackSelector The [TrackSelector] for this preload manager.
 * @param bandwidthMeter The [BandwidthMeter] for this preload manager.
 * @param rendererCapabilitiesListFactory The [RendererCapabilitiesList.Factory] for this preload manager.
 * @property allocator The [Allocator] for this preload manager. Have to be the same as the one used by the Player.
 * @param playbackThread The [Thread] on which the players run. Its lifecycle is handled internally by [PillarboxPreloadManager].
 *
 * @see DefaultPreloadManager
 */
class PillarboxPreloadManager(
    context: Context,
    targetPreloadStatusControl: TargetPreloadStatusControl<Int>? = null,
    mediaSourceFactory: PillarboxMediaSourceFactory = PillarboxMediaSourceFactory(context),
    trackSelector: TrackSelector = PillarboxTrackSelector(context),
    bandwidthMeter: BandwidthMeter = PillarboxBandwidthMeter(context),
    rendererCapabilitiesListFactory: RendererCapabilitiesList.Factory = DefaultRendererCapabilitiesList.Factory(
        PillarboxRenderersFactory(context)
    ),
    val allocator: DefaultAllocator = DefaultAllocator(false, C.DEFAULT_BUFFER_SEGMENT_SIZE),
    private val playbackThread: HandlerThread = HandlerThread("PillarboxPreloadManager:Playback", Process.THREAD_PRIORITY_AUDIO),
) {
    private val preloadManager: DefaultPreloadManager

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

    /**
     * Playback looper to use with PillarboxExoPlayer.
     */
    val playbackLooper: Looper

    init {
        playbackThread.start()
        playbackLooper = playbackThread.looper
        trackSelector.init({}, bandwidthMeter)
        preloadManager = DefaultPreloadManager(
            targetPreloadStatusControl ?: DefaultTargetPreloadStatusControl(),
            mediaSourceFactory,
            trackSelector,
            bandwidthMeter,
            rendererCapabilitiesListFactory,
            allocator,
            playbackLooper,
        )
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
     * Release the preload manager.
     * The preload manager must not be used after calling this method.
     *
     * @see DefaultPreloadManager.release
     */
    fun release() {
        preloadManager.release()
        playbackThread.quitSafely()
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
