/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.app.Application
import android.os.HandlerThread
import android.os.Process
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRendererCapabilitiesList
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.Status.STAGE_LOADED_TO_POSITION_MS
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import androidx.media3.exoplayer.upstream.DefaultAllocator
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.player.PillarboxBandwidthMeter
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.PillarboxRenderersFactory
import ch.srgssr.pillarbox.player.PillarboxTrackSelector
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * [ViewModel] that manages multiple [Player]s that can be used in a story-like layout.
 */
class StoryViewModel(application: Application) : AndroidViewModel(application) {

    private val playbackThread: HandlerThread = HandlerThread("StoryMode-playback", Process.THREAD_PRIORITY_AUDIO).apply {
        start()
    }
    private val playbackLooper = playbackThread.looper
    private val allocator = DefaultAllocator(false, C.DEFAULT_BUFFER_SEGMENT_SIZE)
    private val mediaSourceFactory = PillarboxMediaSourceFactory(application).apply {
        addAssetLoader(SRGAssetLoader(application))
    }
    private val loadControl = PillarboxLoadControl(
        bufferDurations = PillarboxLoadControl.BufferDurations(
            minBufferDuration = 5.seconds,
            maxBufferDuration = 20.seconds,
            bufferForPlayback = 500.milliseconds,
            bufferForPlaybackAfterRebuffer = 1_000.milliseconds,
        ),
        allocator
    )

    private val preloadManager =
        DefaultPreloadManager(
            StoryPreloadStatusControl(),
            mediaSourceFactory,
            PillarboxTrackSelector(application).apply {
                init({}, PillarboxBandwidthMeter(application))
            },
            PillarboxBandwidthMeter(application),
            DefaultRendererCapabilitiesList.Factory(PillarboxRenderersFactory(application)),
            allocator,
            playbackLooper,
        )

    private var currentPage = C.INDEX_UNSET

    private val players = SparseArray<PillarboxExoPlayer>(3).apply {
        for (i in 0 until 3) {
            put(
                i,
                PillarboxExoPlayer(
                    context = application,
                    playbackLooper = playbackLooper,
                    loadControl = loadControl
                ).apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    prepare()
                }
            )
        }
    }

    /**
     * Player count
     */
    val playerCount: Int = players.size()

    /**
     * The list of items to play.
     */
    val mediaItems: List<MediaItem> = (Playlist.StoryUrns.items + Playlist.VideoUrns.items).map { it.toMediaItem() }

    init {
        mediaItems.forEachIndexed { index, mediaItem ->
            preloadManager.add(mediaItem, index)
        }
        preloadManager.setCurrentPlayingIndex(0)
        preloadManager.invalidate()

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
        return players[page % players.size()]
    }

    /**
     * Set the current page, do nothing if it is already the current.
     *
     * @param page The current page
     */
    fun setCurrentPage(page: Int) {
        if (currentPage == page) return
        currentPage = page
        preloadManager.setCurrentPlayingIndex(currentPage)
        preloadManager.invalidate()
    }

    /**
     * Play
     *
     * @param player to play all others are paused.
     */
    fun play(player: PillarboxExoPlayer) {
        if (player.playWhenReady) return
        players.forEach { _, value ->
            value.pause()
            value.seekToDefaultPosition()
            if (value == player) {
                player.play()
            }
        }
    }

    override fun onCleared() {
        preloadManager.release()
        players.forEach { _, value ->
            value.release()
        }
        playbackThread.quitSafely()
    }

    /**
     * Default implementation of [TargetPreloadStatusControl] that will preload the first second of the `n ± 1` item, and the first half-second of
     * the `n ± 2,3` item, where `n` is the index of the current item.
     */
    @Suppress("MagicNumber")
    inner class StoryPreloadStatusControl : TargetPreloadStatusControl<Int> {
        override fun getTargetPreloadStatus(rankingData: Int): TargetPreloadStatusControl.PreloadStatus? {
            val offset = abs(rankingData - currentPage)

            return when (offset) {
                1 -> Status(STAGE_LOADED_TO_POSITION_MS, 1.seconds.inWholeMicroseconds)
                2, 3, 4 -> Status(STAGE_LOADED_TO_POSITION_MS, 500.milliseconds.inWholeMicroseconds)
                else -> null
            }
        }
    }
}
