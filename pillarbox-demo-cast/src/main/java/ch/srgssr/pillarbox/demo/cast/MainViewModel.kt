/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.isConnected
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ViewModel that olds current player and handle local to remote player switch.
 *
 * Best result can be achieved with a PillarboxMediaSessionService and with PillarboxMediaController.
 *
 * @param application The application context.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val castPlayer = PillarboxCastPlayer(application)
    private val localPlayer = PillarboxExoPlayer(application)

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val currentPlayer = MutableStateFlow(if (application.getCastContext().isConnected()) castPlayer else localPlayer)

    init {
        castPlayer.setSessionAvailabilityListener(object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                switchPlayer(castPlayer)
            }

            override fun onCastSessionUnavailable() {
                switchPlayer(localPlayer)
            }
        })
    }

    override fun onCleared() {
        castPlayer.release()
        localPlayer.release()
    }

    private fun switchPlayer(player: Player) {
        val oldPlayer = currentPlayer.value
        if (oldPlayer == player) return
        player.repeatMode = oldPlayer.repeatMode
        player.playWhenReady = oldPlayer.playWhenReady
        player.setMediaItems(oldPlayer.getCurrentMediaItems(), oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)
        currentPlayer.value = player

        oldPlayer.stop()
        oldPlayer.clearMediaItems()
    }
}
