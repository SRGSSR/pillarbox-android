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
import ch.srgssr.pillarbox.player.asset.BlockedInterval
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.extension.pillarboxData

/**
 * Blocked interval tracker that seeks to [BlockedInterval.end] when the player reaches the segment.
 */
internal class BlockedIntervalTracker(
    private val pillarboxExoPlayer: PillarboxExoPlayer
) : CurrentMediaItemPillarboxDataTracker.Callback {
    private val listPlayerMessage = mutableListOf<PlayerMessage>()
    private var listBlockedIntervals = emptyList<BlockedInterval>()
    private val listener = Listener()

    override fun onPillarboxDataChanged(mediaItem: MediaItem?, data: PillarboxData?) {
        clearPlayerMessage()
        pillarboxExoPlayer.removeListener(listener)
        if (data == null || mediaItem == null) return
        listBlockedIntervals = mediaItem.pillarboxData.blockedIntervals
        pillarboxExoPlayer.addListener(listener)
        createMessages()
    }

    private fun notifyBlockedSegment(blockedSection: BlockedInterval) {
        Log.i(TAG, "Blocked segment reached $blockedSection")
        pillarboxExoPlayer.notifyBlockedIntervalReached(blockedSection)
        pillarboxExoPlayer.seekToWithoutSmoothSeeking(blockedSection.end + 1)
    }

    private fun createMessages() {
        listBlockedIntervals.forEach {
            val message = pillarboxExoPlayer.createMessage { _, message ->
                val segment = message as BlockedInterval
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
            val blockedInterval = listBlockedIntervals.firstOrNull { player.currentPosition in it }
            blockedInterval?.let {
                notifyBlockedSegment(it)
            }
        }
    }

    companion object {
        private const val TAG = "BlockedSegmentTracker"
    }
}
