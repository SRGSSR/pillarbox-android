/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.TimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemPillarboxDataTracker

internal class BlockedTimeRangeTracker(
    private val callback: (TimeRange?) -> Unit
) : CurrentMediaItemPillarboxDataTracker.Callback, Player.Listener {
    private val playerMessages = mutableListOf<PlayerMessage>()
    private var timeRanges: List<BlockedTimeRange>? = null
    private lateinit var player: PillarboxExoPlayer

    fun setPlayer(player: PillarboxExoPlayer) {
        this.player = player
        player.addListener(this)
    }

    /*
     * Called when callback is added and already have a PillarboxData.
     */
    override fun onPillarboxDataChanged(data: PillarboxData?) {
        clear()
        data?.let {
            timeRanges = it.blockedTimeRanges
            it.blockedTimeRanges.firstOrNullAtPosition(player.currentPosition)?.let { timeRange ->
                callback(timeRange)
            }
            createMessages(it.blockedTimeRanges)
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        val blockedInterval = timeRanges?.firstOrNullAtPosition(player.currentPosition)
        blockedInterval?.let {
            // Ignore blocked time ranges that end at the same time as the media. Otherwise infinite seek operations.
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
        timeRanges = null
    }

    fun release() {
        clear()
    }
}
