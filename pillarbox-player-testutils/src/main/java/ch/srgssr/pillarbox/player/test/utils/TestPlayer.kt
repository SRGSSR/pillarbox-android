/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.test.utils

import androidx.media3.common.Player
import kotlinx.coroutines.delay

class TestPlayer(val player: Player) {

    suspend fun prepare() {
        player.prepare()
        player.waitForPlaybackState(Player.STATE_READY)
    }

    suspend fun play() {
        player.play()
        player.waitIsPlaying()
    }

    suspend fun pause() {
        player.pause()
        player.waitForPause()
    }

    suspend fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        player.waitForPlaybackState(Player.STATE_READY)
    }

    suspend fun release() {
        player.stop()
        player.release()
        player.waitForPlaybackState(Player.STATE_IDLE)
    }

    suspend fun stop() {
        player.stop()
        player.waitForPlaybackState(Player.STATE_IDLE)
    }

    suspend fun waitForCondition(condition: (Player) -> Boolean) {
        player.waitForCondition(condition)
    }

    companion object {
        private const val WAIT_DELAY = 200L

        @Suppress("TooGenericExceptionThrown")
        suspend fun Player.waitForCondition(condition: (Player) -> Boolean) {
            while (!condition(this)) {
                if (playerError != null) throw RuntimeException(playerError)
                delay(WAIT_DELAY)
            }
        }

        suspend fun Player.waitForPlaybackState(state: @Player.State Int) {
            waitForCondition {
                it.playbackState == state
            }
        }

        suspend fun Player.waitForPause() {
            waitForCondition {
                it.playbackState == Player.STATE_READY && !it.playWhenReady
            }
        }

        suspend fun Player.waitIsPlaying() {
            waitForCondition {
                it.isPlaying
            }
        }
    }
}
