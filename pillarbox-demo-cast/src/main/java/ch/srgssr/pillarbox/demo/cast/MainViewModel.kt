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
import ch.srgssr.pillarbox.cast.isCastSessionAvailableAsFlow
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform

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
    val currentPlayer = castPlayer.isCastSessionAvailableAsFlow()
        .map { if (it) castPlayer else localPlayer }
        .distinctUntilChanged()
        .onEach(onFirstValue = ::setupPlayer, onRemainingValues = ::switchPlayer)
        .stateIn(viewModelScope, WhileSubscribed(), if (castPlayer.isCastSessionAvailable()) castPlayer else localPlayer)

    override fun onCleared() {
        castPlayer.release()
        localPlayer.release()
    }

    private fun setupPlayer(player: Player) {
        if (player.mediaItemCount == 0) {
            player.prepare()
            player.play()
        }
    }

    private fun switchPlayer(player: Player) {
        val oldPlayer = if (player == castPlayer) localPlayer else castPlayer
        // Disable switch player because its always start with an empty local player.
        /*
        player.repeatMode = oldPlayer.repeatMode
        player.playWhenReady = oldPlayer.playWhenReady
        if (oldPlayer.mediaItemCount != 0) {
            player.setMediaItems(oldPlayer.getCurrentMediaItems(), oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)
        }*/
        oldPlayer.stop()
        oldPlayer.clearMediaItems()
    }

    private fun <T> Flow<T>.onEach(
        onFirstValue: suspend (value: T) -> Unit,
        onRemainingValues: suspend (value: T) -> Unit,
    ): Flow<T> {
        var first = true
        return transform { value ->
            if (first) {
                onFirstValue(value)
                first = false
            } else {
                onRemainingValues(value)
            }

            emit(value)
        }
    }
}
