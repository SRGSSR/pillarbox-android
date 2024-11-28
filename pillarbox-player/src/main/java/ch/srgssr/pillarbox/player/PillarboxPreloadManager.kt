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
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status.STAGE_LOADED_FOR_DURATION_MS
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Helper class for Media3's [DefaultPreloadManager] that simplifies preloading of media items for [PillarboxExoPlayer].
 *
 * @param context The [Context].
 * @param targetPreloadStatusControl The [TargetPreloadStatusControl] to decide when to preload an item and for how long.
 * @param mediaSourceFactory The [PillarboxMediaSourceFactory] to create each [MediaSource].
 * @param trackSelector The [TrackSelector] for this preload manager.
 * @param bandwidthMeter The [BandwidthMeter] for this preload manager.
 * @param renderersFactory The [RenderersFactory] for this preload manager.
 * @param loadControl The [LoadControl] for this preload manager.
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
    renderersFactory: RenderersFactory = PillarboxRenderersFactory(context),
    loadControl: LoadControl = PillarboxLoadControl(),
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
     * Gets the count of the [MediaSource]s currently being managed by the preload manager.
     *
     * @return The count of the [MediaSource]s.
     * @see DefaultPreloadManager.getSourceCount
     */
    val sourceCount: Int
        get() = preloadManager.sourceCount

    /**
     * The [Looper] associated with the [Thread] on which playback operations are performed by the [PillarboxExoPlayer].
     */
    val playbackLooper: Looper

    init {
        playbackThread.start()
        playbackLooper = playbackThread.looper
        trackSelector.init({}, bandwidthMeter)
        preloadManager = DefaultPreloadManager.Builder(context, targetPreloadStatusControl ?: DefaultTargetPreloadStatusControl())
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelectorFactory { trackSelector }
            .setBandwidthMeter(bandwidthMeter)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setPreloadLooper(playbackLooper)
            .build()
    }

    /**
     * Adds a [MediaItem] with its [rankingData] to the preload manager.
     *
     * @param mediaItem The [MediaItem] to add.
     * @param rankingData The ranking data that is associated with the [mediaItem].
     * @see DefaultPreloadManager.add
     */
    fun add(mediaItem: MediaItem, rankingData: Int) {
        preloadManager.add(mediaItem, rankingData)
    }

    /**
     * Adds a [MediaSource] with its [rankingData] to the preload manager.
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
     * @return The source for the given [mediaItem] if it is managed by the preload manager, `null` otherwise.
     * @see DefaultPreloadManager.getMediaSource
     */
    fun getMediaSource(mediaItem: MediaItem): MediaSource? {
        return preloadManager.getMediaSource(mediaItem)
    }

    /**
     * Invalidates the current preload progress, and triggers a new preload progress based on the new priorities of the managed [MediaSource]s.
     *
     * @see DefaultPreloadManager.invalidate
     */
    fun invalidate() {
        preloadManager.invalidate()
    }

    /**
     * Releases the preload manager.
     * The preload manager must not be used after calling this method.
     *
     * @see DefaultPreloadManager.release
     */
    fun release() {
        preloadManager.release()
        playbackThread.quitSafely()
    }

    /**
     * Removes a [MediaItem] from the preload manager.
     *
     * @param mediaItem The [MediaItem] to remove.
     * @return `true` if the preload manager is holding a [MediaSource] of the given [MediaItem] and it has been removed, `false` otherwise.
     * @see DefaultPreloadManager.remove
     */
    fun remove(mediaItem: MediaItem): Boolean {
        return preloadManager.remove(mediaItem)
    }

    /**
     * Removes a [MediaSource] from the preload manager.
     *
     * @param mediaSource The [MediaSource] to remove.
     * @return `true` if the preload manager is holding the given [MediaSource] and it has been removed, `false` otherwise.
     * @see DefaultPreloadManager.remove
     */
    fun remove(mediaSource: MediaSource): Boolean {
        return preloadManager.remove(mediaSource)
    }

    /**
     * Resets the preload manager. All sources that the preload manager is holding will be released.
     *
     * @see DefaultPreloadManager.reset
     */
    fun reset() {
        preloadManager.reset()
    }

    /**
     * Default implementation of [TargetPreloadStatusControl] that manages the preload status of items based on their proximity to the currently
     * playing item.
     *
     * This implementation uses a simple distance-based strategy:
     * - The item immediately before or after the current item (offset of 1) is preloaded to 1 second.
     * - The items two or three positions away from the current item (offset of 2 or 3) are preloaded to 0.5 seconds.
     * - All other items are not preloaded.
     *
     * This strategy aims to preload content that is likely to be played soon, reducing buffering and improving playback smoothness.
     */
    @Suppress("MagicNumber")
    inner class DefaultTargetPreloadStatusControl : TargetPreloadStatusControl<Int> {
        override fun getTargetPreloadStatus(rankingData: Int): TargetPreloadStatusControl.PreloadStatus? {
            val offset = abs(rankingData - currentPlayingIndex)

            return when (offset) {
                1 -> Status(STAGE_LOADED_FOR_DURATION_MS, 1.seconds.inWholeMilliseconds)
                2, 3 -> Status(STAGE_LOADED_FOR_DURATION_MS, 500.milliseconds.inWholeMilliseconds)
                else -> null
            }
        }
    }
}
