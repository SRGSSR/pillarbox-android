/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition
import ch.srgssr.pillarbox.player.extension.getBlockedTimeRangeOrNull

internal class BlockedTimeRangeTracker(
    private val callback: (BlockedTimeRange) -> Unit
) : Player.Listener {
    private val playerMessages = mutableListOf<PlayerMessage>()
    private var timeRanges: List<BlockedTimeRange>? = null
        set(value) {
            if (field != value) {
                clear()
                field = value
                field?.let {
                    createMessages(it)
                }
            }
        }
    private lateinit var player: PillarboxExoPlayer

    fun setPlayer(player: PillarboxExoPlayer) {
        this.player = player
        timeRanges = player.currentTracks.getBlockedTimeRangeOrNull()
        player.addListener(this)
    }

    override fun onTracksChanged(tracks: Tracks) {
        timeRanges = tracks.getBlockedTimeRangeOrNull()
    }

    override fun onEvents(player: Player, events: Player.Events) {
        val blockedInterval = timeRanges?.firstOrNullAtPosition(player.currentPosition)
        blockedInterval?.let {
            // Ignore blocked time ranges that end at the same time as the media. Otherwise, infinite seeks operations.
            if (player.currentPosition >= player.duration) return@let
            callback(it)
        }
    }

    private fun createMessages(timeRanges: List<BlockedTimeRange>) {
        val target = PlayerMessage.Target { _, message ->
            callback(message as BlockedTimeRange)
        }
        playerMessages.addAll(
            timeRanges.map {
                player.createMessage(target).apply {
                    deleteAfterDelivery = false
                    looper = player.applicationLooper
                    payload = it
                    setPosition(it.start)
                    send()
                }
            }
        )
    }

    private fun clear() {
        playerMessages.forEach { playerMessage ->
            playerMessage.cancel()
        }
        playerMessages.clear()
    }

    fun release() {
        timeRanges = null
    }
}
