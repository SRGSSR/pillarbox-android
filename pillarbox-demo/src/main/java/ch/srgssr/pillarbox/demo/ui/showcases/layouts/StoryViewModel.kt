/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.app.Application
import android.os.HandlerThread
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.upstream.DefaultAllocator
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.source.BlockedTimeRangeAssetLoader
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.PillarboxPreloadManager
import ch.srgssr.pillarbox.player.PlayerPool
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * [ViewModel] that manages multiple [Player]s that can be used in a story-like layout.
 */
class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val playbackThread = HandlerThread("MediaSourceEdge:Playback", Process.THREAD_PRIORITY_AUDIO).apply { start() }
    private val preloadLooper = playbackThread.looper
    private val loadControl = PillarboxLoadControl(
        bufferDurations = PillarboxLoadControl.BufferDurations(
            minBufferDuration = 5.seconds,
            maxBufferDuration = 20.seconds,
            bufferForPlayback = 500.milliseconds,
        ),
        allocator = DefaultAllocator(false, C.DEFAULT_BUFFER_SEGMENT_SIZE),
    )
    private val preloadManager = PillarboxPreloadManager(
        context = application,
        mediaSourceFactory = PillarboxMediaSourceFactory(application).apply {
            addAssetLoader(SRGAssetLoader(application))
        },
        playerPool = PlayerPool(
            playersCount = 3,
            playerFactory = {
                PillarboxExoPlayer(
                    context = application,
                    mediaSourceFactory = PillarboxMediaSourceFactory(application).apply {
                        addAssetLoader(SRGAssetLoader(application))
                        addAssetLoader(BlockedTimeRangeAssetLoader(application))
                    },
                    loadControl = loadControl,
                    playbackLooper = preloadLooper,
                ).apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    prepare()
                }
            },
        ),
    )

    /**
     * The list of items to play.
     */
    val mediaItems: List<MediaItem> = Playlist.VideoUrns.items.map { it.toMediaItem() }

    init {
        mediaItems.forEachIndexed { index, mediaItem ->
            preloadManager.add(mediaItem, index)
        }
        preloadManager.invalidate()
    }

    /**
     * Set the [pageNumber] as the currently active page.
     *
     * @param pageNumber The currently active page.
     */
    fun setActivePage(pageNumber: Int) {
        if (preloadManager.currentPlayingIndex == pageNumber) return
        preloadManager.getCurrentlyPlayingPlayer()?.pause()
        preloadManager.currentPlayingIndex = pageNumber
        preloadManager.invalidate()
        preloadManager.getCurrentlyPlayingPlayer()?.play()
    }

    /**
     * Get the [PillarboxExoPlayer] instance for page [pageNumber], with its media source set.
     *
     * @param pageNumber The page number.
     */
    fun getConfiguredPlayerForPageNumber(pageNumber: Int): PillarboxExoPlayer {
        val mediaSource = checkNotNull(preloadManager.getMediaSource(mediaItems[pageNumber]))
        val player = checkNotNull(preloadManager.getPlayer(pageNumber))
        player.setMediaSource(mediaSource)

        return player
    }

    override fun onCleared() {
        super.onCleared()
        preloadManager.release()
        playbackThread.quit()
    }
}
