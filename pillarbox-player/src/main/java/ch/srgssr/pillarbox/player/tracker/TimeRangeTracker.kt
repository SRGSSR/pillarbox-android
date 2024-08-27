/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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
import ch.srgssr.pillarbox.player.extension.chapters
import ch.srgssr.pillarbox.player.extension.credits

internal class TimeRangeTracker(
    private val pillarboxPlayer: PillarboxExoPlayer,
    private val callback: Callback
) : CurrentMediaItemPillarboxDataTracker.Callback {

    interface Callback {
        fun onChapterChanged(chapter: Chapter?)
        fun onCreditChanged(credit: Credit?)
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
        createMessages(mediaItem.mediaMetadata, data.blockedTimeRanges)
    }

    private fun createMessages(mediaMetadata: MediaMetadata, timeRanges: List<BlockedTimeRange>) {
        val position = pillarboxPlayer.currentPosition
        if (timeRanges.isNotEmpty()) {
            listTrackers.add(
                BlockedTimeRangeTracker(
                    initialPosition = position,
                    timeRanges = timeRanges,
                    callback = callback::onBlockedTimeRange
                )
            )
        }

        mediaMetadata.chapters?.let { chapters ->
            if (chapters.isNotEmpty()) {
                listTrackers.add(
                    ChapterCreditsTracker(
                        initialPosition = position,
                        timeRanges = chapters,
                        callback = callback::onChapterChanged
                    )
                )
            }
        }

        mediaMetadata.credits?.let { credits ->
            if (credits.isNotEmpty()) {
                listTrackers.add(
                    ChapterCreditsTracker(
                        initialPosition = position,
                        timeRanges = credits,
                        callback = callback::onCreditChanged
                    )
                )
            }
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
            it.clear()
        }
        listTrackers.clear()
    }
}

private sealed interface PlayerTimeRangeTracker<T : TimeRange> : PillarboxPlayer.Listener {
    fun createMessages(player: PillarboxExoPlayer): List<PlayerMessage>
    fun clear() {}
}

private class ChapterCreditsTracker<T : TimeRange>(
    initialPosition: Long,
    private val timeRanges: List<T>,
    private val callback: (T?) -> Unit,
) : PlayerTimeRangeTracker<T> {

    private var currentTimeRange: T? = null
        set(value) {
            if (field != value) {
                callback(value)
                field = value
            }
        }

    init {
        currentTimeRange = timeRanges.firstOrNullAtPosition(initialPosition)
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        @DiscontinuityReason reason: Int,
    ) {
        if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
            clear()
        } else {
            val currentPosition = newPosition.positionMs
            val currentTimeRange = currentTimeRange
                ?.takeIf { timeRange -> currentPosition in timeRange }
                ?: timeRanges.firstOrNullAtPosition(currentPosition)
            this.currentTimeRange = currentTimeRange
        }
    }

    override fun createMessages(player: PillarboxExoPlayer): List<PlayerMessage> {
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

    override fun clear() {
        currentTimeRange = null
    }

    private companion object {
        private const val TYPE_ENTER = 1
        private const val TYPE_EXIT = 2
    }
}

/**
 * Whenever a [BlockedTimeRange] is reached, [callback] has to be called because the player will skip the content to the end.
 */
private class BlockedTimeRangeTracker(
    initialPosition: Long,
    private val timeRanges: List<BlockedTimeRange>,
    private val callback: (BlockedTimeRange) -> Unit
) : PlayerTimeRangeTracker<BlockedTimeRange> {

    init {
        timeRanges.firstOrNullAtPosition(initialPosition)?.let {
            callback(it)
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        val blockedInterval = timeRanges.firstOrNullAtPosition(player.currentPosition)
        blockedInterval?.let {
            // Ignore blocked time ranges that end at the same time as the media. Otherwise infinite seek operations.
            if (player.currentPosition >= player.duration) return@let
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
