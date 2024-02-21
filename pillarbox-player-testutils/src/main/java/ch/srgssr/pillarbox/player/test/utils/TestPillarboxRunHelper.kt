/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.test.utils

import android.os.Looper
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.Clock
import androidx.media3.common.util.ConditionVariable
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.robolectric.RobolectricUtil
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

object TestPillarboxRunHelper {
    private fun verifyMainTestThread(player: Player) {
        check(!(Looper.myLooper() != Looper.getMainLooper() || player.applicationLooper != Looper.getMainLooper()))
    }

    private fun verifyPlaybackThreadIsAlive(player: ExoPlayer) {
        Assertions.checkState(player.playbackLooper.thread.isAlive, "Playback thread is not alive, has the player been released?")
    }

    /**
     * Runs tasks of the main Looper until [Player.Listener.onEvents] matches the
     * expected state or a playback error occurs.
     *
     * <p>If a playback error occurs it will be thrown wrapped in an {@link IllegalStateException}.
     *
     * @param player The [Player].
     * @param expectedEvent The expected [Player.Event] if null wait until first [Player.Listener.onEvents].
     * @throws TimeoutException If the [RobolectricUtil.DEFAULT_TIMEOUT_MS] is exceeded.
     */
    @Throws(TimeoutException::class)
    fun runUntilEvent(player: Player, expectedEvent: @Player.Event Int? = null) {
        verifyMainTestThread(player)
        if (player is ExoPlayer) {
            verifyPlaybackThreadIsAlive(player)
        }
        val receivedCallback = AtomicBoolean(false)
        val listener: Player.Listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (expectedEvent?.let { events.contains(it) } != false) {
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
     * <p>If a playback error occurs it will be thrown wrapped in an {@link IllegalStateException}.
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
     * Same as [TestPlayerRunHelper.playUntilStartOfMediaItem], but doesn't pause the player afterwards.
     *
     * @param player The [Player].
     * @param mediaItemIndex The index of the media item.
     *
     * @throws TimeoutException If the [default timeout][RobolectricUtil.DEFAULT_TIMEOUT_MS] is exceeded.
     *
     * @see TestPlayerRunHelper.playUntilStartOfMediaItem
     */
    @Throws(TimeoutException::class)
    fun runUntilStartOfMediaItem(player: ExoPlayer, mediaItemIndex: Int) {
        verifyMainTestThread(player)
        verifyPlaybackThreadIsAlive(player)

        val applicationLooper = Util.getCurrentOrMainLooper()
        val messageHandled = AtomicBoolean(false)

        player
            .createMessage { _, _ ->
                // Block playback thread until pause command has been sent from test thread.
                val blockPlaybackThreadCondition = ConditionVariable()

                player.clock
                    .createHandler(applicationLooper, null)
                    .post {
                        messageHandled.set(true)
                        blockPlaybackThreadCondition.open()
                    }

                try {
                    player.clock.onThreadBlocked()
                    blockPlaybackThreadCondition.block()
                } catch (e: InterruptedException) {
                    // Ignore.
                }
            }
            .setPosition(mediaItemIndex, 0L)
            .send()
        player.play()
        RobolectricUtil.runMainLooperUntil { messageHandled.get() || player.playerError != null }
        if (player.playerError != null) {
            throw IllegalStateException(player.playerError)
        }
    }
}
