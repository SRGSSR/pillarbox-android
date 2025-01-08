/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.TimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition
import ch.srgssr.pillarbox.player.extension.chapters
import ch.srgssr.pillarbox.player.extension.credits

internal class PillarboxMediaMetaDataTracker(private val callback: (TimeRange?) -> Unit) : Player.Listener {
    private var currentChapterTracker: Tracker<Chapter>? = null
    private var currentCreditTracker: Tracker<Credit>? = null
    private lateinit var player: PillarboxExoPlayer

    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
        when (reason) {
            Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> {
                if (oldPosition.mediaItemIndex == newPosition.mediaItemIndex) {
                    val position = newPosition.positionMs
                    currentCreditTracker?.setCurrentPosition(position)
                    currentChapterTracker?.setCurrentPosition(position)
                } else {
                    release()
                }
            }

            else -> {
                release()
            }
        }
    }

    fun setPlayer(player: PillarboxExoPlayer) {
        this.player = player
        this.player.addListener(this)
    }

    fun release() {
        currentChapterTracker?.clear()
        currentChapterTracker = null

        currentCreditTracker?.clear()
        currentCreditTracker = null
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        mediaMetadata.chapters?.let {
            if (currentChapterTracker?.timeRanges != it) {
                currentChapterTracker?.clear()
                currentChapterTracker = Tracker(player = player, timeRanges = it, callback = callback)
            }
        }

        mediaMetadata.credits?.let {
            if (currentCreditTracker?.timeRanges != it) {
                currentCreditTracker?.clear()
                currentCreditTracker = Tracker(player = player, timeRanges = it, callback = callback)
            }
        }
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
            currentTimeRange = currentTimeRange
                ?.takeIf { timeRange -> currentPosition in timeRange }
                ?: timeRanges.firstOrNullAtPosition(currentPosition)
        }

        private fun createMessages(): List<PlayerMessage> {
            return buildList(timeRanges.size * 2) {
                timeRanges.forEach { timeRange ->
                    val messageEnter = player.createMessage { _, _ ->
                        currentTimeRange = timeRange
                    }.setPosition(timeRange.start)
                    val messageExit = player.createMessage { _, _ ->
                        val nextTimeRange = timeRanges.firstOrNullAtPosition(player.currentPosition)
                        if (nextTimeRange == null) {
                            currentTimeRange = null
                        }
                    }.setPosition(timeRange.end)

                    add(messageEnter)
                    add(messageExit)
                }
            }.onEach { message ->
                message.deleteAfterDelivery = false
                message.looper = player.applicationLooper
                message.send()
            }
        }

        fun clear() {
            currentTimeRange = null
            messages.forEach { it.cancel() }
        }
    }
}
