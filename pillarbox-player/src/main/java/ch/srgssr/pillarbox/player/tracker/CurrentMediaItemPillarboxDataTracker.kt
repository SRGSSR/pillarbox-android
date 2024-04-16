/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.extension.getPillarboxDataOrNull

/**
 * This class detects when the [PillarboxData] stored as a `tag` of the current [MediaItem] changes. You can be notified of each change by
 * providing a custom `Callback` to this class:
 * ```kotlin
 * val mediaItemPillarboxDataTracker = CurrentMediaItemPillarboxDataTracker(player)
 * mediaItemPillarboxDataTracker.addCallback(object : CurrentMediaItemPillarboxDataTracker.Callback {
 *     override fun onPillarboxDataChanged(mediaItem: MediaItem?, data: PillarboxData?) {
 *         // The PillarboxData of the current `MediaItem` has changed
 *     }
 * })
 * ```
 *
 * @param player The [Player] for which the current media item's tag must be tracked.
 */
internal class CurrentMediaItemPillarboxDataTracker(private val player: ExoPlayer) {
    interface Callback {
        /**
         * Called when the [PillarboxData] of the current media item changes.
         *
         * @param mediaItem The current [MediaItem].
         * @param data The [PillarboxData] of the current [MediaItem]. Might be `null` if no [PillarboxData] is set.
         */
        fun onPillarboxDataChanged(
            mediaItem: MediaItem?,
            data: PillarboxData?,
        )
    }

    /**
     * The callbacks managed by this tracker.
     */
    private val callbacks = mutableSetOf<Callback>()

    private var lastMediaId: String? = null
    private var lastTag: PillarboxData? = null

    init {
        player.addListener(CurrentMediaItemListener())
    }

    fun addCallback(callback: Callback) {
        callbacks.add(callback)

        // If the player already has a MediaItem set, let the new callback know about its current tag
        player.currentMediaItem?.let { mediaItem ->
            val tag = mediaItem.getPillarboxDataOrNull()

            callback.onPillarboxDataChanged(mediaItem, tag)
        }
    }

    private fun notifyPillarboxDataChange(mediaItem: MediaItem?) {
        val mediaId = mediaItem?.mediaId
        val tag = mediaItem.getPillarboxDataOrNull()
        // Only send the tag if either the media id or the tag have changed
        if (lastMediaId == mediaId && lastTag == tag) {
            return
        }

        callbacks.forEach { callback ->
            callback.onPillarboxDataChanged(mediaItem, tag)
        }

        lastMediaId = mediaId
        lastTag = tag
    }

    private inner class CurrentMediaItemListener : Player.Listener {
        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            @Player.MediaItemTransitionReason reason: Int,
        ) {
            if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
                notifyPillarboxDataChange(mediaItem)
            }
        }

        override fun onTimelineChanged(
            timeline: Timeline,
            @Player.TimelineChangeReason reason: Int,
        ) {
            notifyPillarboxDataChange(player.currentMediaItem)
        }
    }
}
