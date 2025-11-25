/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.app.Application
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.PreloadStatus
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.PillarboxPreloadManager
import ch.srgssr.pillarbox.player.PillarboxTrackSelector
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * [ViewModel] that manages multiple [Player]s that can be used in a story-like layout.
 */
class StoryViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaSourceFactory = PillarboxMediaSourceFactory(application).apply {
        addAssetLoader(SRGAssetLoader(application))
    }
    private val loadControl = PillarboxLoadControl(
        bufferDurations = PillarboxLoadControl.BufferDurations(
            minBufferDuration = 5.seconds,
            maxBufferDuration = 20.seconds,
            bufferForPlayback = 500.milliseconds,
            bufferForPlaybackAfterRebuffer = 1.seconds,
        ),
    )
    private val preloadManager = PillarboxPreloadManager(
        context = application,
        targetPreloadStatusControl = StoryPreloadStatusControl(),
        mediaSourceFactory = mediaSourceFactory,
        trackSelector = PillarboxTrackSelector(application).apply {
            parameters = parameters.buildUpon()
                .setForceLowestBitrate(true)
                .build()
        },
        loadControl = loadControl,
    )

    private var currentPage = C.INDEX_UNSET

    private val players = SparseArray<PillarboxExoPlayer>(PLAYERS_COUNT).apply {
        for (i in 0 until PLAYERS_COUNT) {
            val player = PillarboxExoPlayer(application) {
                playbackLooper(preloadManager.playbackLooper)
                loadControl(loadControl)
            }.apply {
                repeatMode = Player.REPEAT_MODE_ONE
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                prepare()
            }

            put(i, player)
        }
    }

    /**
     * Player count
     */
    val playerCount: Int = players.size

    /**
     * The list of items to play.
     */
    val mediaItems: List<MediaItem> = (SamplesSRG.StoryVideoUrns.items).map { it.toMediaItem() }

    init {
        mediaItems.forEachIndexed { index, mediaItem ->
            preloadManager.add(mediaItem, index)
        }
        setCurrentPage(0)

        players.forEach { key, _ -> setupPlayerForPage(key) }
    }

    /**
     * Set up player for the [page].
     *
     * @param page The page.
     */
    fun setupPlayerForPage(page: Int) {
        val player = getPlayer(page)
        val mediaSource = getMediaSourceForPage(page)
        if (mediaSource.mediaItem == player.currentMediaItem) return
        player.setMediaSource(mediaSource)
        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
    }

    private fun getMediaSourceForPage(page: Int): MediaSource = checkNotNull(preloadManager.getMediaSource(mediaItems[page]))

    /**
     * @param page The page.
     * @return the player for the [page].
     */
    fun getPlayer(page: Int): PillarboxExoPlayer {
        return players[page % playerCount]
    }

    /**
     * Set the current page, do nothing if it is already the current.
     *
     * @param page The current page
     */
    fun setCurrentPage(page: Int) {
        if (currentPage == page) return
        currentPage = page
        preloadManager.currentPlayingIndex = currentPage
    }

    /**
     * Play
     *
     * @param player The player to play, all others are paused.
     */
    fun play(player: PillarboxExoPlayer) {
        if (player.playWhenReady) return
        players.forEach { _, value ->
            value.seekToDefaultPosition()
            value.playWhenReady = value == player
        }
    }

    override fun onCleared() {
        players.forEach { _, value ->
            value.release()
        }
        preloadManager.release()
    }

    /**
     * Custom implementation of [TargetPreloadStatusControl] that will preload the first second of the `n ± 1` item, and the first millisecond of
     * the `n ± 2,3,4` item, where `n` is the index of the current item.
     */
    @Suppress("MagicNumber")
    private inner class StoryPreloadStatusControl : TargetPreloadStatusControl<Int, PreloadStatus> {
        override fun getTargetPreloadStatus(rankingData: Int): PreloadStatus {
            val offset = abs(rankingData - currentPage)

            return when (offset) {
                1 -> PreloadStatus.specifiedRangeLoaded(1.seconds.inWholeMilliseconds)
                2, 3, 4 -> PreloadStatus.specifiedRangeLoaded(1.milliseconds.inWholeMilliseconds)
                else -> PreloadStatus.PRELOAD_STATUS_NOT_PRELOADED
            }
        }
    }

    private companion object {
        private const val PLAYERS_COUNT = 3
    }
}
