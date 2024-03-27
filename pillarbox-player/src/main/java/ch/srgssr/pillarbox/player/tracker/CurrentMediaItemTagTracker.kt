/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer

/**
 * This class detects when the `tag` of the current [MediaItem] changes. You can be notified of each change by providing a custom `Callback` to
 * this class:
 * ```kotlin
 * val mediaItemTagTracker = CurrentMediaItemTagTracker(player)
 * mediaItemTagTracker.addCallback(object : CurrentMediaItemTagTracker.Callback {
 *     override fun onTagChanged(mediaItem: MediaItem?, tag: Any?) {
 *         // The tag of the current `MediaItem` has changed
 *     }
 * })
 * ```
 *
 * @param player The [Player] for which the current media item's tag must be tracked.
 */
internal class CurrentMediaItemTagTracker(private val player: ExoPlayer) {
    interface Callback {
        /**
         * Called when the tag of the current media item changes.
         *
         * @param mediaItem The current [MediaItem].
         * @param tag The tag of the current [MediaItem]. Might be `null` if no tag is set.
         */
        fun onTagChanged(
            mediaItem: MediaItem?,
            tag: Any?,
        )
    }

    /**
     * The callbacks managed by this tracker.
     */
    private val callbacks = mutableSetOf<Callback>()

    private var lastMediaId: String? = null
    private var lastTag: Any? = null

    init {
        player.addListener(CurrentMediaItemListener())
    }

    fun addCallback(callback: Callback) {
        callbacks.add(callback)

        // If the player already has a MediaItem set, let the new callback know about its current tag
        player.currentMediaItem?.let { mediaItem ->
            val tag = mediaItem.localConfiguration?.tag

            callback.onTagChanged(mediaItem, tag)
        }
    }

    private fun notifyTagChange(mediaItem: MediaItem?) {
        val mediaId = mediaItem?.mediaId
        val tag = mediaItem?.localConfiguration?.tag
        // Only send the tag if either the media id or the tag have changed
        if (lastMediaId == mediaId && lastTag == tag) {
            return
        }

        callbacks.forEach { callback ->
            callback.onTagChanged(mediaItem, tag)
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
                notifyTagChange(mediaItem)
            }
        }

        override fun onTimelineChanged(
            timeline: Timeline,
            @Player.TimelineChangeReason reason: Int,
        ) {
            notifyTagChange(player.currentMediaItem)
        }
    }
}
