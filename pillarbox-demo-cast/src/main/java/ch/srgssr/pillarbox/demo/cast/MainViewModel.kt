/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.isCastSessionAvailableAsFlow
import ch.srgssr.pillarbox.cast.isConnected
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

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
    val currentPlayer = castPlayer.isCastSessionAvailableAsFlow().map {
        if (it) castPlayer else localPlayer
    }
        .distinctUntilChanged()
        .onEach { switchPlayer(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            if (application.getCastContext().isConnected()) castPlayer else localPlayer
        )

    override fun onCleared() {
        castPlayer.release()
        localPlayer.release()
    }

    private fun switchPlayer(player: Player) {
        val oldPlayer = if (player is PillarboxCastPlayer) localPlayer else castPlayer
        if (oldPlayer == player) return
        player.repeatMode = oldPlayer.repeatMode
        player.playWhenReady = oldPlayer.playWhenReady
        player.setMediaItems(oldPlayer.getCurrentMediaItems(), oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)

        oldPlayer.stop()
        oldPlayer.clearMediaItems()
    }
}
