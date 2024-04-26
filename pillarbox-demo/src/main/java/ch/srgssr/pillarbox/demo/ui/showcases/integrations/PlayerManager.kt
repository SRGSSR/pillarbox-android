/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import androidx.compose.runtime.Stable
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manage a local player (PillarboxExoPlayer) and a remote controller (CastPlayer)
 */
@Stable
class PlayerManager(
    private val localPlayer: PillarboxPlayer,
    castContext: CastContext,
) {
    private var remotePlayer: CastPlayer = CastPlayer(castContext, PillarboxMediaItemConverter())
    private val listItems = localPlayer.getCurrentMediaItems().toMutableList()

    // FIXME should check first if cast player is casting!
    private var currentPlayer: MutableStateFlow<Player> = MutableStateFlow(localPlayer)
    val player = currentPlayer.asStateFlow()

    init {
        remotePlayer.setSessionAvailabilityListener(SessionListener())
    }

    fun release() {
        remotePlayer.setSessionAvailabilityListener(null)
        remotePlayer.release()
        localPlayer.release()
    }

    /**
     * Add MediaItem to current player and store it.
     * Maybe we could listen playlist change?
     */
    fun addMediaItem(mediaItem: MediaItem) {
        listItems.add(mediaItem)
        currentPlayer.value.addMediaItem(mediaItem)
    }

    private fun setCurrentPlayer(player: Player) {
        if (currentPlayer.value == player) return
        val oldPlayer = currentPlayer.value

        player.playWhenReady = oldPlayer.playWhenReady
        player.setMediaItems(listItems, oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)
        player.prepare()

        oldPlayer.stop()

        currentPlayer.value = player
    }

    inner class SessionListener : SessionAvailabilityListener {
        override fun onCastSessionAvailable() {
            setCurrentPlayer(remotePlayer)
        }

        override fun onCastSessionUnavailable() {
            setCurrentPlayer(localPlayer)
        }
    }
}
