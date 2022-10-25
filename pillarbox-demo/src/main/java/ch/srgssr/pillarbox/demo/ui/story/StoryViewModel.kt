/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.story

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.upstream.DefaultAllocator
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srg.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
import ch.srgssr.pillarbox.demo.data.DemoPlaylistProvider
import ch.srgssr.pillarbox.demo.data.MixedMediaItemSource
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Story view model
 */
class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaItemSource = MixedMediaItemSource(
        MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(application, IlHost.PROD))
    )

    /**
     * Players
     */
    private val players = arrayOf(
        PillarboxPlayer(context = application, mediaItemSource = mediaItemSource, loadControl = fastStartPlaybackLoadControl),
        PillarboxPlayer(context = application, mediaItemSource = mediaItemSource, loadControl = fastStartPlaybackLoadControl),
        PillarboxPlayer(context = application, mediaItemSource = mediaItemSource, loadControl = fastStartPlaybackLoadControl)
    )

    /**
     * Playlist to use with viewpager
     */
    val playlist: Playlist

    init {
        playlist = DemoPlaylistProvider(application).loadDemoItemFromAssets("playlists.json")[INDEX]
        preparePlayers()
    }

    /**
     * Set current page, will seek to the releated media item
     *
     * @param pageNumber
     */
    fun setCurrentPage(pageNumber: Int) {
        val demoItem = playlist.items[pageNumber]
        val player = getPlayerForPageNumber(pageNumber)
        var itemToSelect: Int? = null
        for (i in 0 until player.mediaItemCount) {
            val item = player.getMediaItemAt(i)
            if (item.mediaId == demoItem.uri) {
                itemToSelect = i
                break
            }
        }
        itemToSelect?.let {
            player.seekToDefaultPosition(itemToSelect)
        }
    }

    /**
     * Get player for page number
     *
     * @param pageNumber
     * @return [PillarboxPlayer] that should be used for this [pageNumber]
     */
    fun getPlayerForPageNumber(pageNumber: Int): PillarboxPlayer {
        val playerIndex = pageNumber % players.size
        return players[playerIndex]
    }

    private fun preparePlayers() {
        players.forEachIndexed { index, player ->
            for (i in index until playlist.items.size step players.size) {
                player.addMediaItem(playlist.items[i].toMediaItem())
            }
            // player.pauseAtEndOfMediaItems = true
            player.repeatMode = Player.REPEAT_MODE_ONE // Repeat endlessly the current item.
            player.prepare()
        }
    }

    override fun onCleared() {
        super.onCleared()
        for (player in players) {
            player.stop()
            player.release()
        }
    }

    companion object {
        private const val INDEX = 1 // 0 for urn 1 for urls

        // Minimum Video you want to buffer while Playing
        private const val MIN_BUFFER_DURATION = 3000

        // Max Video you want to buffer during PlayBack
        private const val MAX_BUFFER_DURATION = 10_000

        // Min Video you want to buffer before start Playing it
        private const val MIN_PLAYBACK_START_BUFFER = 1000

        // Min video You want to buffer when user resumes video
        private const val MIN_PLAYBACK_RESUME_BUFFER = 1000

        private val fastStartPlaybackLoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                MIN_BUFFER_DURATION,
                MAX_BUFFER_DURATION,
                MIN_PLAYBACK_START_BUFFER,
                MIN_PLAYBACK_RESUME_BUFFER
            )
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }
}
