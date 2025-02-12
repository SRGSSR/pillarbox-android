/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.isConnected
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems

/**
 * ViewModel that olds current player and handle local to remote player switch.
 *
 * Best result can be achieved with a PillarboxMediaSessionService and with PillarboxMediaController.
 *
 * @param application The application context.
 */
class MainViewModel(application: Application) : AndroidViewModel(application), SessionAvailabilityListener {
    private val castPlayer: PillarboxCastPlayer = PillarboxCastPlayer(application)
    private val localPlayer = PillarboxExoPlayer(application)
    private var _currentPlayer: PillarboxPlayer by mutableStateOf(if (application.getCastContext().isConnected()) castPlayer else localPlayer)

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val currentPlayer: PillarboxPlayer
        get() = _currentPlayer
    private var listItems: List<MediaItem>
    private val itemTracking = ItemsTracking()

    init {
        castPlayer.setSessionAvailabilityListener(this)
        if (currentPlayer.mediaItemCount == 0) {
            val mediaItems = listOf(
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML,
                DemoItem.GoogleDashH265_CENC_Widewine,
                DemoItem.OnDemandAudio,
                DemoItem.OnDemandAudioMP3,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.DvrVideo,
            ).map { it.toMediaItem() }
            currentPlayer.setMediaItems(mediaItems)
            currentPlayer.prepare()
            currentPlayer.play()
        }
        listItems = _currentPlayer.getCurrentMediaItems()
        _currentPlayer.addListener(itemTracking)
    }

    override fun onCleared() {
        castPlayer.setSessionAvailabilityListener(null)
        castPlayer.release()
        localPlayer.release()
    }

    private fun setPlayer(player: PillarboxPlayer) {
        if (_currentPlayer == player) return
        val oldPlayer = _currentPlayer
        oldPlayer.removeListener(itemTracking)
        player.addListener(itemTracking)

        val playWhenReady = oldPlayer.playWhenReady
        val currentMediaItemIndex = oldPlayer.currentMediaItemIndex
        val currentPosition = oldPlayer.currentPosition

        player.playWhenReady = playWhenReady
        player.setMediaItems(listItems, currentMediaItemIndex, currentPosition)
        player.prepare()
        _currentPlayer = player
        oldPlayer.stop()
    }

    override fun onCastSessionAvailable() {
        setPlayer(castPlayer)
    }

    override fun onCastSessionUnavailable() {
        setPlayer(localPlayer)
    }

    private inner class ItemsTracking : PillarboxPlayer.Listener {

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            super.onTimelineChanged(timeline, reason)
            // Currently when restoring from a CastPlayer, the playlist is cleared. It might be fixed in a future version of Media3.
            listItems = _currentPlayer.getCurrentMediaItems()
        }
    }
}
