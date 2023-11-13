/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.multiplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule

/**
 * Multi player view model
 *
 * Two players playing content endlessly.
 * There is no audio focus and audio volume handle for this demo.
 */
class MultiPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val player1 = PlayerModule.provideDefaultPlayer(application)
    private val player2 = PlayerModule.provideDefaultPlayer(application)

    init {
        /*
         * On some devices playing DRM content on multiple players may not work.
         * One of the players will receive a PlaybackException with ERROR_CODE_DECODER_INIT_FAILED.
         * It may happen on low-end devices like Samsung Galaxy A13, for example.
         */
        player1.setMediaItem(DemoItem.LiveVideo.toMediaItem())
        player2.setMediaItem(DemoItem.DvrVideo.toMediaItem())
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
