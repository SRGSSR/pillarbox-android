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
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.TimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition

internal class TimeRangeTracker(
    private val pillarboxPlayer: PillarboxExoPlayer,
    private val callback: Callback
) : CurrentMediaItemPillarboxDataTracker.Callback {

    interface Callback {
        fun onChapterChanged(chapter: Chapter?)
        fun onCreditsChanged(credit: Credit?)
        fun onBlockedTimeRange(blockedTimeRange: BlockedTimeRange)
    }

    private val playerMessages = mutableListOf<PlayerMessage>()
    private val listTrackers = mutableListOf<PlayerTimeRangeTracker<*>>()

    override fun onPillarboxDataChanged(mediaItem: MediaItem?, data: PillarboxData?) {
        clearPlayerMessages()

        if (data == null || mediaItem == null) {
            // set current item to null
            return
        }
        createMessages(data)
    }

    private fun createMessages(data: PillarboxData) {
        val position = pillarboxPlayer.currentPosition
        if (data.blockedTimeRanges.isNotEmpty()) {
            listTrackers.add(
                BlockedTimeRangeTracker(
                    initialPosition = position,
                    timeRanges = data.blockedTimeRanges,
                    callback = callback::onBlockedTimeRange
                )
            )
        }
        if (data.chapters.isNotEmpty()) {
            listTrackers.add(
                ChapterCreditsTracker(
                    initialPosition = position,
                    timeRanges = data.chapters,
                    callback = callback::onChapterChanged
                )
            )
        }

        if (data.credits.isNotEmpty()) {
            listTrackers.add(
                ChapterCreditsTracker(
                    initialPosition = position,
                    timeRanges = data.credits,
                    callback = callback::onCreditsChanged
                )
            )
        }

        listTrackers.forEach {
            pillarboxPlayer.addListener(it)
            playerMessages.addAll(it.createMessages(pillarboxPlayer))
        }
    }

    private fun clearPlayerMessages() {
        playerMessages.forEach { playerMessage ->
            playerMessage.cancel()
        }
        playerMessages.clear()

        listTrackers.forEach {
            pillarboxPlayer.removeListener(it)
        }
        listTrackers.clear()
    }
}

internal sealed interface PlayerTimeRangeTracker<T : TimeRange> : PillarboxPlayer.Listener {
    fun createMessages(player: PillarboxExoPlayer): List<PlayerMessage>
}

internal class ChapterCreditsTracker<T : TimeRange>(
    initialPosition: Long,
    val timeRanges: List<T>,
    val callback: (T?) -> Unit,
) : PlayerTimeRangeTracker<T> {

    private var currentTimeRange: T? = timeRanges.firstOrNullAtPosition(initialPosition)
        set(value) {
            if (field != value) {
                callback(value)
                field = value
            }
        }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        @DiscontinuityReason reason: Int,
    ) {
        if (
            (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) &&
            oldPosition.mediaItemIndex == newPosition.mediaItemIndex
        ) {
            val currentPosition = oldPosition.positionMs
            val currentTimeInterval = currentTimeRange
                ?.takeIf { timeInterval -> currentPosition in timeInterval }
                ?: (timeRanges.firstOrNullAtPosition(currentPosition))

            this.currentTimeRange = currentTimeInterval
        }
    }

    override fun createMessages(player: PillarboxExoPlayer): List<PlayerMessage> {
        val messageHandler = PlayerMessage.Target { messageType, message ->
            @Suppress("UNCHECKED_CAST")
            val timeRange = message as? T ?: return@Target
            when (messageType) {
                TYPE_ENTER -> { currentTimeRange = timeRange }

                TYPE_EXIT -> currentTimeRange = null
            }
        }
        val playerMessages = mutableListOf<PlayerMessage>()
        timeRanges.forEach { timeInterval ->
            val messageEnter = player.createMessage(messageHandler).apply {
                deleteAfterDelivery = false
                looper = player.applicationLooper
                payload = timeInterval
                setPosition(timeInterval.start)
                type = TYPE_ENTER
                send()
            }
            val messageExit = player.createMessage(messageHandler).apply {
                deleteAfterDelivery = false
                looper = player.applicationLooper
                payload = timeInterval
                setPosition(timeInterval.end)
                type = TYPE_EXIT
                send()
            }
            playerMessages.add(messageEnter)
            playerMessages.add(messageExit)
        }
        return playerMessages
    }

    private companion object {
        private const val TYPE_ENTER = 1
        private const val TYPE_EXIT = 2
    }
}

/**
 * Whenever a [BlockedTimeRange] is reach [callback] have to be called each time because,
 * player will skip the content to the end.
 */
internal class BlockedTimeRangeTracker(
    initialPosition: Long,
    val timeRanges: List<BlockedTimeRange>,
    val callback: (BlockedTimeRange) -> Unit
) : PlayerTimeRangeTracker<BlockedTimeRange> {

    init {
        timeRanges.firstOrNullAtPosition(initialPosition)?.let {
            callback(it)
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        val blockedInterval = timeRanges.firstOrNullAtPosition(player.currentPosition)
        blockedInterval?.let {
            callback(it)
        }
    }

    override fun createMessages(player: PillarboxExoPlayer): List<PlayerMessage> {
        val target = PlayerMessage.Target { _, message ->
            callback(message as BlockedTimeRange)
        }
        return timeRanges.map {
            player.createMessage(target).apply {
                deleteAfterDelivery = false
                looper = player.applicationLooper
                payload = it
                setPosition(it.start)
                send()
            }
        }
    }
}
