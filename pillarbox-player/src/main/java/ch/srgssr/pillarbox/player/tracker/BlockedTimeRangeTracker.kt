/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.extension.getBlockedTimeRangeAtPosition
import ch.srgssr.pillarbox.player.extension.pillarboxData

/**
 * Blocked range tracker that seeks to [BlockedTimeRange.end] when the player reaches the segment.
 */
internal class BlockedTimeRangeTracker(
    private val pillarboxExoPlayer: PillarboxExoPlayer
) : CurrentMediaItemPillarboxDataTracker.Callback {
    private val listPlayerMessage = mutableListOf<PlayerMessage>()
    private var listBlockedTimeRangeIntervals = emptyList<BlockedTimeRange>()
    private val listener = Listener()

    override fun onPillarboxDataChanged(mediaItem: MediaItem?, data: PillarboxData?) {
        clearPlayerMessage()
        pillarboxExoPlayer.removeListener(listener)
        if (data == null || mediaItem == null) return
        listBlockedTimeRangeIntervals = mediaItem.pillarboxData.blockedTimeRanges
        pillarboxExoPlayer.addListener(listener)
        createMessages()
    }

    private fun notifyBlockedSegment(blockedTimeRangeSection: BlockedTimeRange) {
        Log.i(TAG, "Blocked segment reached $blockedTimeRangeSection")
        pillarboxExoPlayer.notifyBlockedTimeRangeReached(blockedTimeRangeSection)
        pillarboxExoPlayer.seekToWithoutSmoothSeeking(blockedTimeRangeSection.end + 1)
    }

    private fun createMessages() {
        listBlockedTimeRangeIntervals.forEach {
            val message = pillarboxExoPlayer.createMessage { _, message ->
                val segment = message as BlockedTimeRange
                notifyBlockedSegment(segment)
            }.apply {
                deleteAfterDelivery = false
                looper = pillarboxExoPlayer.applicationLooper
                payload = it
                setPosition(it.start)
            }
            message.send()
            listPlayerMessage.add(message)
        }
    }

    private fun clearPlayerMessage() {
        listPlayerMessage.forEach {
            it.cancel()
        }
        listPlayerMessage.clear()
    }

    private inner class Listener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            val blockedInterval = player.getBlockedTimeRangeAtPosition(player.currentPosition)
            blockedInterval?.let {
                notifyBlockedSegment(it)
            }
        }
    }

    companion object {
        private const val TAG = "BlockedSegmentTracker"
    }
}
