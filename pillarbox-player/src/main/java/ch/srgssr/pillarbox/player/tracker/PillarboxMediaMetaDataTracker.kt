/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
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

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        when (reason) {
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK,
            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
                clear()
                mediaItem?.mediaMetadata?.let { mediaMetadata ->
                    mediaMetadata.chapters?.let {
                        currentChapterTracker = Tracker(player = player, timeRanges = it, callback = callback)
                    }
                    mediaMetadata.credits?.let {
                        currentCreditTracker = Tracker(player = player, timeRanges = it, callback = callback)
                    }
                }
            }

            else -> Unit
        }
    }

    fun setPlayer(player: PillarboxExoPlayer) {
        this.player = player
        this.player.addListener(this)
    }

    fun release() {
        clear()
    }

    /**
     * This callback isn't call again if the next or previous item has the same MediaMetadata.
     */
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
