/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.asset.TimeRange
import ch.srgssr.pillarbox.player.extension.pillarboxData

internal class TimeRangeTracker<T : TimeRange>(
    private val player: PillarboxExoPlayer,
    private val getTimeIntervalAtPosition: PillarboxExoPlayer.(position: Long) -> T?,
    private val getAllTimeIntervals: PillarboxData.() -> List<T>,
    private val notifyTimeIntervalChanged: PillarboxExoPlayer.(timeInterval: T?) -> Unit,
) : CurrentMediaItemPillarboxDataTracker.Callback {
    private val playerMessages = mutableListOf<PlayerMessage>()
    private var timeRanges = emptyList<T>()
    private val listener = Listener()

    private var lastTimeInterval: T? = player.getTimeIntervalAtPosition(player.currentPosition)
        set(value) {
            if (field != value) {
                field = value
                player.notifyTimeIntervalChanged(field)
            }
        }

    override fun onPillarboxDataChanged(mediaItem: MediaItem?, data: PillarboxData?) {
        clearPlayerMessages()
        lastTimeInterval = player.getTimeIntervalAtPosition(player.currentPosition)
        player.removeListener(listener)
        if (data == null || mediaItem == null) {
            return
        }

        timeRanges = mediaItem.pillarboxData.getAllTimeIntervals()
        player.addListener(listener)
        createPlayerMessages()
    }

    private fun createPlayerMessages() {
        val messageHandler = PlayerMessage.Target { messageType, message ->
            @Suppress("UNCHECKED_CAST")
            val timeInterval = message as? T ?: return@Target

            when (messageType) {
                TYPE_ENTER -> {
                    if (timeInterval != lastTimeInterval) {
                        lastTimeInterval = timeInterval
                    }
                }

                TYPE_EXIT -> lastTimeInterval = null
            }
        }

        timeRanges.forEach { timeInterval ->
            val messageEnter = player.createMessage(messageHandler).apply {
                deleteAfterDelivery = false
                looper = player.applicationLooper
                payload = timeInterval
                setPosition(timeInterval.start)
                type = TYPE_ENTER
            }
            val messageExit = player.createMessage(messageHandler).apply {
                deleteAfterDelivery = false
                looper = player.applicationLooper
                payload = timeInterval
                setPosition(timeInterval.end)
                type = TYPE_EXIT
            }

            messageEnter.send()
            messageExit.send()

            playerMessages.add(messageEnter)
            playerMessages.add(messageExit)
        }
    }

    private fun clearPlayerMessages() {
        playerMessages.forEach { playerMessage ->
            playerMessage.cancel()
        }
        playerMessages.clear()
    }

    private inner class Listener : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @DiscontinuityReason reason: Int,
        ) {
            if (
                (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) &&
                oldPosition.mediaItemIndex == newPosition.mediaItemIndex
            ) {
                val currentPosition = player.currentPosition
                val currentTimeInterval = lastTimeInterval
                    ?.takeIf { timeInterval -> currentPosition in timeInterval }
                    ?: player.getTimeIntervalAtPosition(currentPosition)

                if (currentTimeInterval != lastTimeInterval) {
                    lastTimeInterval = currentTimeInterval
                }
            }
        }
    }

    private companion object {
        private const val TYPE_ENTER = 1
        private const val TYPE_EXIT = 2
    }
}
