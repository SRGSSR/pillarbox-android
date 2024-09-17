/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.extension.getPillarboxDataOrNull

/**
 *
 * @param player The [Player] for which the current media item's tag must be tracked.
 */
internal class CurrentMediaItemPillarboxDataTracker(private val player: ExoPlayer) {
    interface Callback {
        /**
         * Called when the [PillarboxData] of the current media item changes.
         *
         * @param data The [PillarboxData] of the current [MediaItem]. Might be `null` if no [PillarboxData] is set.
         */
        fun onPillarboxDataChanged(
            data: PillarboxData?,
        )
    }

    /**
     * The callbacks managed by this tracker.
     */
    private val callbacks = mutableSetOf<Callback>()
    private var currentPillarboxData: PillarboxData? = player.currentTracks.getPillarboxDataOrNull()
        set(value) {
            // Check instance instead of content, because multiple items could have the same data.
            if (field !== value) {
                notifyPillarboxDataChange(value)
                field = value
            }
        }

    init {
        player.addListener(CurrentMediaItemListener())
    }

    /**
     * To be called when [Player.release].
     */
    fun release() {
        currentPillarboxData = null
    }

    /**
     * Add callback will call [Callback.onPillarboxDataChanged] with the current [PillarboxData] if not `null`.
     */
    fun addCallback(callback: Callback) {
        callbacks.add(callback)
        currentPillarboxData?.let {
            callback.onPillarboxDataChanged(it)
        }
    }

    private fun notifyPillarboxDataChange(pillarboxData: PillarboxData?) {
        callbacks.forEach { callback ->
            callback.onPillarboxDataChanged(pillarboxData)
        }
    }

    private inner class CurrentMediaItemListener : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {
            currentPillarboxData = tracks.getPillarboxDataOrNull()
        }
    }
}
