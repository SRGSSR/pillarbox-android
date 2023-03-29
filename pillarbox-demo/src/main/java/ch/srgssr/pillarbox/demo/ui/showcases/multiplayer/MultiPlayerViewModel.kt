/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.multiplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.di.Dependencies

/**
 * Multi player view model
 *
 * Two players playing content endlessly.
 * There is no audio focus and audio volume handle for this demo.
 */
class MultiPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val player1 = Dependencies.provideDefaultPlayer(application)
    private val player2 = Dependencies.provideDefaultPlayer(application)

    init {
        player1.setMediaItem(MediaItem.Builder().setMediaId("urn:rts:video:6820736").build())
        player2.setMediaItem(MediaItem.fromUri("https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"))
        preparePlayer(player1)
        preparePlayer(player2)
    }

    private fun preparePlayer(player: Player) {
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.play()
    }

    /**
     * Get player left
     *
     * @param playerSwap
     */
    fun getPlayerLeft(playerSwap: Boolean): Player {
        return if (playerSwap) {
            player1
        } else {
            player2
        }
    }

    /**
     * Get player right
     *
     * @param playerSwap
     * @return
     */
    fun getPlayerRight(playerSwap: Boolean): Player {
        return getPlayerLeft(!playerSwap)
    }

    override fun onCleared() {
        super.onCleared()
        player1.release()
        player2.release()
    }
}
