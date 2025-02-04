/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.isConnected
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.SRGMediaItemConverter
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
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val castPlayer: PillarboxCastPlayer
    private val localPlayer = PillarboxExoPlayer(application)
    private var _currentPlayer: MutableState<PillarboxPlayer>

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val currentPlayer: State<PillarboxPlayer>
        get() = _currentPlayer
    private var listItems: List<MediaItem>
    private val listener = CastSessionListener()

    init {
        val castContext = application.getCastContext()
        castPlayer =
            PillarboxCastPlayer(
                castContext = castContext,
                context = application,
                mediaItemConverter = SRGMediaItemConverter()
            )
        _currentPlayer = mutableStateOf(if (castContext.isConnected()) castPlayer else localPlayer)
        castPlayer.setSessionAvailabilityListener(listener)
        if (currentPlayer.value.mediaItemCount == 0) {
            val mediaItems = listOf(
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML,
                DemoItem.GoogleDashH265_CENC_Widewine,
                DemoItem.OnDemandAudio,
                DemoItem.OnDemandAudioMP3,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.DvrVideo,
            ).map { it.toMediaItem() }
            currentPlayer.value.setMediaItems(mediaItems)
            currentPlayer.value.prepare()
            currentPlayer.value.play()
        }
        listItems = _currentPlayer.value.getCurrentMediaItems()
    }

    override fun onCleared() {
        castPlayer.setSessionAvailabilityListener(null)
        castPlayer.release()
        localPlayer.release()
    }

    private fun setPlayer(player: PillarboxPlayer) {
        if (_currentPlayer.value == player) return
        val oldPlayer = _currentPlayer.value
        oldPlayer.removeListener(listener)
        player.addListener(listener)

        val playWhenReady = oldPlayer.playWhenReady
        val currentMediaItemIndex = oldPlayer.currentMediaItemIndex
        val currentPosition = oldPlayer.currentPosition

        player.playWhenReady = playWhenReady
        player.setMediaItems(listItems, currentMediaItemIndex, currentPosition)
        player.prepare()
        _currentPlayer.value = player
        oldPlayer.stop()
    }

    private inner class CastSessionListener : SessionAvailabilityListener, PillarboxPlayer.Listener {
        override fun onCastSessionAvailable() {
            setPlayer(castPlayer)
        }

        override fun onCastSessionUnavailable() {
            setPlayer(localPlayer)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            super.onTimelineChanged(timeline, reason)
            // Currently when restoring from a CastPlayer, the playlist is cleared. It might be fixe in next version of Media3.
            listItems = _currentPlayer.value.getCurrentMediaItems()
        }
    }
}
