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
import ch.srgssr.pillarbox.player.asset.BlockedSection
import ch.srgssr.pillarbox.player.extension.pillarboxData

/**
 * Blocked segment tracker that seek to [BlockedSection.end] when player reach the segment.
 */
class BlockedIntervalTracker(private val pillarboxExoPlayer: PillarboxExoPlayer) : CurrentMediaItemTagTracker.Callback {
    private val listPlayerMessage = mutableListOf<PlayerMessage>()
    private var listBlockedIntervals = emptyList<BlockedSection>()
    private val listener = Listener()

    override fun onTagChanged(mediaItem: MediaItem?, tag: Any?) {
        clearPlayerMessage()
        pillarboxExoPlayer.removeListener(listener)
        if (tag == null || mediaItem == null) return
        listBlockedIntervals = mediaItem.pillarboxData.blockedIntervals
        pillarboxExoPlayer.addListener(listener)
        createMessages()
    }

    private fun notifyBlockedSegment(blockedSection: BlockedSection) {
        Log.i(TAG, "Blocked segment reached $blockedSection")
        pillarboxExoPlayer.seekToWithoutSmoothSeeking(blockedSection.end + 1)
    }

    private fun createMessages() {
        listBlockedIntervals.forEach {
            val message = pillarboxExoPlayer.createMessage { messageType, message ->
                val segment = message as BlockedSection
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
            val blockedSection = listBlockedIntervals.firstOrNull { player.currentPosition in it }
            blockedSection?.let {
                notifyBlockedSegment(it)
            }
        }
    }

    companion object {
        private const val TAG = "BlockedSegmentTracker"
    }
}
