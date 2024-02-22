/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Segment
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker

/**
 * Blocked segment tracker that seek to [Segment.markOut] when player reach the segment.
 */
class BlockedSegmentTracker : MediaItemTracker {

    /**
     * Blocked segment
     *
     * @property list A list of blocked [Segment]
     */
    data class BlockedSegment(val list: List<Segment>)

    private var _player: ExoPlayer? = null
    private val player: ExoPlayer
        get() {
            return _player!!
        }
    private var data: BlockedSegment? = null
    private val componentListener = ComponentListener()
    private val listPlayerMessage = mutableListOf<PlayerMessage>()
    private val blockedSegmentList = emptyList<Segment>()

    override fun start(player: ExoPlayer, initialData: Any?) {
        data = initialData as? BlockedSegment
        this._player = player
        data?.let {
            setBlockedSegmentList(it.list)
            getSegmentAt(player.currentPosition)?.let { segment ->
                player.seekTo(segment.markOut)
            }
        }
        player.addListener(componentListener)
    }

    override fun update(data: Any) {
        val newData = data as BlockedSegment
        if (newData != this.data) {
            clearPlayerMessages()
            this.data = newData
            setBlockedSegmentList(newData.list)
        }
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        clearPlayerMessages()
        player.removeListener(componentListener)
        _player = null
    }

    private fun getSegmentAt(positionMs: Long): Segment? {
        for (segment in data!!.list) {
            if (positionMs >= segment.markIn && positionMs < segment.markOut) {
                return segment
            }
        }
        return null
    }

    private fun clearPlayerMessages() {
        for (message in listPlayerMessage) {
            message.cancel()
        }
        listPlayerMessage.clear()
    }

    private fun setBlockedSegmentList(segments: List<Segment>) {
        clearPlayerMessages()
        /*
        * Message a send at the given position when the player position is automatically reached
        * The message is send again if player reach again the same position
        */
        val target = PlayerMessage.Target { _, message ->
            val payload = message as Segment
            player.seekTo(payload.markOut)
        }
        val blockedSegmentList = segments.filter { it.blockReason != null }
        for (segment in blockedSegmentList) {
            val message = player.createMessage(target).apply {
                deleteAfterDelivery = false
                looper = player.applicationLooper
                payload = segment
                setPosition(segment.markIn)
            }.send()
            listPlayerMessage.add(message)
        }
    }

    private inner class ComponentListener : Player.Listener {

        override fun onEvents(player: Player, events: Player.Events) {
            val blockedSegment = this@BlockedSegmentTracker.getSegmentAt(player.currentPosition)
            blockedSegment?.let {
                // TODO if the blocked segment is at the begin, seek to the previous item?
                player.seekTo(it.markOut)
            }
        }
    }

    /**
     * Factory
     */
    class Factory : MediaItemTracker.Factory {
        override fun create(): MediaItemTracker {
            return BlockedSegmentTracker()
        }
    }

    companion object {
        private const val TAG = "BlockedSegmentTracker"
    }
}
