/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPreloadManager
import ch.srgssr.pillarbox.player.PlayerPool
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory

/**
 * [ViewModel] that manages multiple [Player]s that can be used in a story-like layout.
 */
class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val preloadManager = PillarboxPreloadManager(
        context = application,
        mediaSourceFactory = PillarboxMediaSourceFactory(application).apply {
            addAssetLoader(SRGAssetLoader(application))
        },
        playerPool = PlayerPool(
            playersCount = 3,
            playerFactory = {
                PlayerModule.provideDefaultPlayer(application).apply {
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
    }
}
