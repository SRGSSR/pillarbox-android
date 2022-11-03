/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.util.Log
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Player controller
 *
 * Handle a local player (PillarboxPlayer) and a remote player (CastPlayer)
 *
 * @property localPlayer
 * @property castContext
 * @constructor Create empty Player controller
 */
class PlayerController(private val localPlayer: PillarboxPlayer, private val castContext: CastContext) : SessionAvailabilityListener {
    private val remotePlayer = CastPlayer(castContext)
    private val _currentPlayer = MutableStateFlow<Player>(localPlayer)

    /**
     * Current player flow
     */
    val currentPlayer: StateFlow<Player> = _currentPlayer

    init {
        remotePlayer.setSessionAvailabilityListener(this)
        setCurrentPlayer(if (remotePlayer.isCastSessionAvailable) remotePlayer else localPlayer)
    }

    private fun setCurrentPlayer(player: Player) {
        if (player == currentPlayer.value) {
            return
        }
        val oldPlayer = currentPlayer.value

        // Player state management.
        val playbackPositionMs: Long = oldPlayer.currentPosition
        val currentItemIndex: Int = oldPlayer.currentMediaItemIndex
        val playWhenReady = oldPlayer.playWhenReady
        val listItems = MutableList<MediaItem>(oldPlayer.mediaItemCount) {
            oldPlayer.getMediaItemAt(it)
        }
        Log.d("Coucou", "add item ${oldPlayer.currentMediaItem?.mediaId} uri = ${oldPlayer.currentMediaItem?.localConfiguration?.uri}")
        oldPlayer.stop()
        oldPlayer.clearMediaItems()

        player.setMediaItems(listItems, currentItemIndex, playbackPositionMs)
        player.playWhenReady = playWhenReady
        player.prepare()

        _currentPlayer.value = player
    }

    /**
     * Add media items
     *
     * @param mediaItems
     */
    fun addMediaItems(mediaItems: List<MediaItem>) {
        currentPlayer.value.addMediaItems(mediaItems)
    }

    /**
     * Play
     */
    fun play() {
        currentPlayer.value.play()
    }

    /**
     * Pause
     */
    fun pause() {
        currentPlayer.value.pause()
    }

    override fun onCastSessionAvailable() {
        setCurrentPlayer(remotePlayer)
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(localPlayer)
    }

    /**
     * Release remote and local player
     */
    fun release() {
        remotePlayer.release()
        remotePlayer.setSessionAvailabilityListener(null)

        localPlayer.release()
    }
}
