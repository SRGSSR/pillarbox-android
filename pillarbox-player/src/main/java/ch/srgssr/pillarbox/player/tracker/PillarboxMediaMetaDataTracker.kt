/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.TimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition

internal class PillarboxMediaMetaDataTracker(
    private val onChapterChange: (Chapter?) -> Unit,
    private val onCreditChange: (Credit?) -> Unit,
) : PillarboxPlayer.Listener {
    private var currentChapterTracker: Tracker<Chapter>? = null
    private var currentCreditTracker: Tracker<Credit>? = null
    private lateinit var player: PillarboxExoPlayer

    private fun clear() {
        currentChapterTracker?.clear()
        currentCreditTracker?.clear()

        currentChapterTracker = null
        currentCreditTracker = null
    }

    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
        when (reason) {
            Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> {
                if (oldPosition.mediaItemIndex == newPosition.mediaItemIndex) {
                    val position = newPosition.positionMs
                    currentCreditTracker?.setCurrentPosition(position)
                    currentChapterTracker?.setCurrentPosition(position)
                }
            }

            else -> Unit
        }
    }

    override fun onPillarboxMetadataChanged(pillarboxMetadata: PillarboxMetadata) {
        if (currentChapterTracker?.timeRanges != pillarboxMetadata.chapters) {
            currentChapterTracker?.clear()
            currentChapterTracker = Tracker(player = player, timeRanges = pillarboxMetadata.chapters, callback = onChapterChange)
        }

        if (currentCreditTracker?.timeRanges != pillarboxMetadata.credits) {
            currentCreditTracker?.clear()
            currentCreditTracker = Tracker(player = player, timeRanges = pillarboxMetadata.credits, callback = onCreditChange)
        }
    }

    fun setPlayer(player: PillarboxExoPlayer) {
        this.player = player
        this.player.addListener(this)
    }

    fun release() {
        clear()
    }

    private class Tracker<T : TimeRange>(
        private val player: PillarboxExoPlayer,
        val timeRanges: List<T>,
        private val callback: (T?) -> Unit,
    ) {
        private val messages: List<PlayerMessage> = createMessages()

        private var currentTimeRange: T? = null
            set(value) {
                if (field != value) {
                    callback(value)
                    field = value
                }
            }

        init {
            currentTimeRange = timeRanges.firstOrNullAtPosition(player.currentPosition)
        }

        fun setCurrentPosition(currentPosition: Long) {
            val currentTimeRange = currentTimeRange
                ?.takeIf { timeRange -> currentPosition in timeRange }
                ?: timeRanges.firstOrNullAtPosition(currentPosition)
            this.currentTimeRange = currentTimeRange
        }

        private fun createMessages(): List<PlayerMessage> {
            val messageHandler = PlayerMessage.Target { messageType, message ->
                @Suppress("UNCHECKED_CAST")
                val timeRange = message as? T ?: return@Target
                when (messageType) {
                    TYPE_ENTER -> currentTimeRange = timeRange
                    TYPE_EXIT -> {
                        val nextTimeRange = timeRanges.firstOrNullAtPosition(player.currentPosition)
                        if (nextTimeRange == null) currentTimeRange = null
                    }
                }
            }
            val playerMessages = mutableListOf<PlayerMessage>()
            timeRanges.forEach { timeRange ->
                val messageEnter = player.createMessage(messageHandler).apply {
                    deleteAfterDelivery = false
                    looper = player.applicationLooper
                    payload = timeRange
                    setPosition(timeRange.start)
                    type = TYPE_ENTER
                    send()
                }
                val messageExit = player.createMessage(messageHandler).apply {
                    deleteAfterDelivery = false
                    looper = player.applicationLooper
                    payload = timeRange
                    setPosition(timeRange.end)
                    type = TYPE_EXIT
                    send()
                }
                playerMessages.add(messageEnter)
                playerMessages.add(messageExit)
            }
            return playerMessages
        }

        fun clear() {
            messages.forEach { it.cancel() }
            currentTimeRange = null
        }

        private companion object {
            private const val TYPE_ENTER = 1
            private const val TYPE_EXIT = 2
        }
    }
}
