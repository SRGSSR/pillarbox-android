/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.test.utils

import android.annotation.SuppressLint
import android.os.Looper
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.RobolectricUtil
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration

object TestPillarboxRunHelper {
    private fun verifyMainTestThread(player: Player) {
        check(!(Looper.myLooper() != Looper.getMainLooper() || player.applicationLooper != Looper.getMainLooper()))
    }

    private fun verifyPlaybackThreadIsAlive(player: ExoPlayer) {
        Assertions.checkState(player.playbackLooper.thread.isAlive, "Playback thread is not alive, has the player been released?")
    }

    /**
     * Runs tasks of the main [Looper] until [Player.Listener.onEvents] matches the
     * expected state or a playback error occurs.
     *
     * If a playback error occurs, it will be thrown wrapped in an [IllegalStateException].
     *
     * @param player The [Player].
     * @param expectedEvents The expected [Player.Event]. If empty, waits until the first [Player.Listener.onEvents].
     * @throws TimeoutException If the [RobolectricUtil.DEFAULT_TIMEOUT_MS] is exceeded.
     */
    @Throws(TimeoutException::class)
    fun runUntilEvents(player: Player, vararg expectedEvents: @Player.Event Int) {
        verifyMainTestThread(player)
        if (player is ExoPlayer) {
            verifyPlaybackThreadIsAlive(player)
        }
        val receivedCallback = AtomicBoolean(false)
        val listener: Player.Listener = object : Player.Listener {
            @SuppressLint("WrongConstant")
            override fun onEvents(player: Player, events: Player.Events) {
                if (expectedEvents.isEmpty() || events.containsAny(*expectedEvents)) {
                    receivedCallback.set(true)
                }
            }
        }
        player.addListener(listener)
        RobolectricUtil.runMainLooperUntil({ receivedCallback.get() || player.playerError != null }, 20_000, Clock.DEFAULT)
        player.removeListener(listener)
        if (player.playerError != null) {
            throw IllegalStateException(player.playerError)
        }
    }

    /**
     * Runs tasks of the main Looper until [Player.Listener.onPlaybackParametersChanged] is called or a playback error occurs.
     *
     * If a playback error occurs, it will be thrown wrapped in an [IllegalStateException].
     *
     * @param player The [Player].
     * @throws TimeoutException If the [RobolectricUtil.DEFAULT_TIMEOUT_MS] is exceeded.
     */
    @Throws(TimeoutException::class)
    fun runUntilPlaybackParametersChanged(player: Player) {
        verifyMainTestThread(player)
        if (player is ExoPlayer) {
            verifyPlaybackThreadIsAlive(player)
        }
        val receivedCallback = AtomicBoolean(false)
        val listener: Player.Listener = object : Player.Listener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                receivedCallback.set(true)
            }
        }
        player.addListener(listener)
        RobolectricUtil.runMainLooperUntil { receivedCallback.get() || player.playerError != null }
        player.removeListener(listener)
        if (player.playerError != null) {
            throw IllegalStateException(player.playerError)
        }
    }

    /**
     * Run and wait until [position] is reached
     *
     * @param player The [Player].
     * @param position The position to wait for.
     * @param clock The [FakeClock].
     */
    @Throws(TimeoutException::class)
    fun runUntilPosition(player: Player, position: Duration, clock: FakeClock) {
        verifyMainTestThread(player)
        if (player is ExoPlayer) {
            verifyPlaybackThreadIsAlive(player)
        }
        clock.advanceTime(position.inWholeMilliseconds)
        RobolectricUtil.runMainLooperUntil {
            player.currentPosition >= position.inWholeMilliseconds
        }
    }

    /**
     * Run and wait until [Player.isPlaying] is [isPlaying].

     * If a playback error occurs, it will be thrown wrapped in an [IllegalStateException].
     *
     * @param player The [Player].
     * @param isPlaying The expected value of [Player.isPlaying].

     * @throws TimeoutException If the [RobolectricUtil.DEFAULT_TIMEOUT_MS] is exceeded.
     */
    @Throws(TimeoutException::class)
    fun runUntilIsPlaying(player: Player, isPlaying: Boolean) {
        verifyMainTestThread(player)
        if (player is ExoPlayer) {
            verifyPlaybackThreadIsAlive(player)
        }
        val receivedCallback = AtomicBoolean(false)
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(actual: Boolean) {
                if (actual == isPlaying) {
                    receivedCallback.set(true)
                }
            }
        }
        player.addListener(listener)
        RobolectricUtil.runMainLooperUntil { receivedCallback.get() || player.playerError != null }
        player.removeListener(listener)
        if (player.playerError != null) {
            throw IllegalStateException(player.playerError)
        }
    }
}
