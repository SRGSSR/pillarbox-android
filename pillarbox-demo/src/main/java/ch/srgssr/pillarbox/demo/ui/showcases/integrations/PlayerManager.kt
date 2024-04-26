/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.utils.StringUtil
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

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
        remotePlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(
                    "Coucou",
                    "Cast:onPlaybackStateChanged ${StringUtil.playerStateString(playbackState)} current = ${remotePlayer.currentMediaItemIndex}"
                )
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d("Coucou", "Cast:MediaItemTransition to ${remotePlayer.currentMediaItemIndex}")
            }
        })
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
        Log.d("Coucou", "$oldPlayer => $player")

        val playWhenReady = oldPlayer.playWhenReady
        val mediaItemIndex = oldPlayer.currentMediaItemIndex
        val position = oldPlayer.currentPosition

        Log.d("Coucou", "mediaItemIndex = $mediaItemIndex pos = ${position.milliseconds}")
        player.playWhenReady = playWhenReady
        player.setMediaItems(listItems, mediaItemIndex, position)
        player.prepare()

        currentPlayer.value = player

        oldPlayer.stop()
        oldPlayer.clearMediaItems()
    }

    inner class SessionListener : SessionAvailabilityListener {
        override fun onCastSessionAvailable() {
            setCurrentPlayer(remotePlayer)
        }

        override fun onCastSessionUnavailable() {

            Log.d("Coucou", "CastUnavailable ${remotePlayer.currentPosition} ${remotePlayer.currentMediaItemIndex}")
            setCurrentPlayer(localPlayer)
        }
    }
}
