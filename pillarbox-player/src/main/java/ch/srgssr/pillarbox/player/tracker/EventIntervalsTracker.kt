/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.util.Log
import androidx.media3.common.Player.Events
import androidx.media3.common.util.ListenerSet
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.TimeInterval
import ch.srgssr.pillarbox.player.getEventIntervals

class EventIntervalsTracker : MediaItemTracker {
    private val playerMessages = mutableListOf<PlayerMessage>()
    private var listenerSet: ListenerSet<PillarboxExoPlayer.Listener>? = null

    override fun start(player: ExoPlayer, initialData: Any?) {
        val eventIntervals = player.getEventIntervals()

        if (player is PillarboxExoPlayer) {
            listenerSet = ListenerSet<PillarboxExoPlayer.Listener>(player.applicationLooper, player.clock) { listener, eventFlags ->
                listener.onEvents(player, Events(eventFlags))
            }.apply {
                player.getPillarboxListeners()
                    .forEach { add(it) }
            }
        }

        Log.d("Coucou", "Chapter trackers start with $eventIntervals")

        /*
        * Message sent at the given position when the player position is automatically reached
        * The message is sent again if the player reaches the same position again
        */
        val enterEventTarget = PlayerMessage.Target { _, message ->
            val eventInterval = message as? TimeInterval ?: return@Target

            Log.d("Coucou", "Enter event $eventInterval")

            listenerSet?.sendEvent(MESSAGE_ENTER_EVENT) {
                it.onEnterTimeInterval(eventInterval)
            }
        }
        val exitEventTarget = PlayerMessage.Target { _, message ->
            val eventInterval = message as? TimeInterval ?: return@Target

            Log.d("Coucou", "Enter event $eventInterval")

            listenerSet?.sendEvent(MESSAGE_ENTER_EVENT) {
                it.onEnterTimeInterval(eventInterval)
            }
        }

        // Setup player messages for each event interval
        for (eventInterval in eventIntervals) {
            val enterEventMessage = player.createMessage(enterEventTarget)
                .setLooper(player.applicationLooper)
                .setPayload(eventInterval)
                .setType(MESSAGE_ENTER_EVENT)
                .setDeleteAfterDelivery(false)
                .setPosition(eventInterval.start)
                .send()
            val exitEventMessage = player.createMessage(exitEventTarget)
                .setLooper(player.applicationLooper)
                .setPayload(eventInterval)
                .setType(MESSAGE_EXIT_EVENT)
                .setDeleteAfterDelivery(false)
                .setPosition(eventInterval.end)
                .send()

            playerMessages.add(enterEventMessage)
            playerMessages.add(exitEventMessage)
        }
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        clearPlayerMessages()
        listenerSet?.release()
    }

    private fun clearPlayerMessages() {
        for (message in playerMessages) {
            message.cancel()
        }
        playerMessages.clear()
    }

    private companion object {
        private const val MESSAGE_ENTER_EVENT = 1
        private const val MESSAGE_EXIT_EVENT = 2
    }
}
