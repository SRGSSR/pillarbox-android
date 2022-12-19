/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.story

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.data.DemoPlaylistProvider
import ch.srgssr.pillarbox.demo.data.Dependencies
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.player.PillarboxPlayer
import kotlin.math.ceil

/**
 * Story view model
 *
 * 3 Players that interleaved DemoItems
 */
class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaItemSource = Dependencies.provideMixedItemSource(application)

    /**
     * Players
     */
    private val players = arrayOf(
        PillarboxPlayer(context = application, mediaItemSource = mediaItemSource, loadControl = StoryLoadControl.build()),
        PillarboxPlayer(context = application, mediaItemSource = mediaItemSource, loadControl = StoryLoadControl.build()),
        PillarboxPlayer(context = application, mediaItemSource = mediaItemSource, loadControl = StoryLoadControl.build())
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
     * Get player for page number
     *
     * @param pageNumber
     * @return [PillarboxPlayer] that should be used for this [pageNumber]
     */
    fun getPlayerForPageNumber(pageNumber: Int): PillarboxPlayer {
        return players[playerIndex(pageNumber)]
    }

    private fun preparePlayers() {
        players.forEachIndexed { index, player ->
            for (i in index until playlist.items.size step players.size) {
                player.addMediaItem(playlist.items[i].toMediaItem())
            }
            player.repeatMode = Player.REPEAT_MODE_ONE // Repeat endlessly the current item.
            player.prepare()
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }
    }

    override fun onCleared() {
        super.onCleared()
        for (player in players) {
            player.stop()
            player.release()
        }
    }

    /**
     * Pause all player
     */
    fun pauseAllPlayer() {
        for (player in players) {
            player.pause()
        }
    }

    /**
     * Get player and media item index for page
     *      I0  I1  I2  I3
     * P0   0   3   6   9
     * P1   1   4   7   10
     * P2   2   5   8   11
     *
     * @param page
     * @return Pair<Index of player,IndexOfItemForPlayer>
     */
    fun getPlayerAndMediaItemIndexForPage(page: Int): Pair<Int, Int> {
        val playerMaxItemCount = playerMaxItemCount()
        val i = playerIndex(page)
        val j = (page - i) / playerMaxItemCount
        return Pair(i, j)
    }

    /**
     * Get player from index
     *
     * @param playerIndex the index received from [getPlayerAndMediaItemIndexForPage]
     */
    fun getPlayerFromIndex(playerIndex: Int) = players[playerIndex]

    /**
     * Seek to the player index to the item index
     *
     * @param playerIndexItemIndex the index received from [getPlayerAndMediaItemIndexForPage]
     */
    fun seekTo(playerIndexItemIndex: Pair<Int, Int>) {
        val player = getPlayerFromIndex(playerIndexItemIndex.first)
        val mediaItemIndex = playerIndexItemIndex.second
        if ((player.currentMediaItem ?: -1) != mediaItemIndex) {
            player.seekToDefaultPosition(mediaItemIndex)
        }
    }

    private fun playerIndex(pageNumber: Int) = pageNumber % players.size

    private fun playerMaxItemCount() = ceil(playlist.items.size / players.size.toFloat()).toInt()

    companion object {
        private const val INDEX = 1 // 0 for urn 1 for urls
    }
}
