/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.player.utils.DebugLogger

/**
 * Reloads the current [MediaItem] when it switches from live (or DVR) to VOD, keeping its position and its play/pause state.
 *
 * Use case: the user watches a live event with a delay (they paused it or went back in time). When the event ends, the stream becomes a VOD.
 * The [MediaItem] is then loaded again by its [ch.srgssr.pillarbox.player.asset.AssetLoader], to get its VOD metadata, trackers, blocked time
 * ranges, ...
 *
 * The [MediaItem] is reloaded without its [MediaMetadata], because asset loaders give priority to the [MediaMetadata] of the [MediaItem] over the
 * loaded one, and the current [MediaItem] contains the metadata loaded for the live.
 *
 * @param player The [Player] to observe.
 */
internal class LiveToVodReloader(private val player: Player) : Player.Listener {
    private val window = Timeline.Window()

    /**
     * The uid of the current window, while it is live.
     */
    private var liveWindowUid: Any? = null

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        liveWindowUid = null
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (timeline.isEmpty) {
            liveWindowUid = null
            return
        }
        timeline.getWindow(player.currentMediaItemIndex, window)
        // A placeholder window is used while the content loads, it doesn't know yet whether the content is live.
        if (window.isPlaceholder) return

        // When an HLS live ends (#EXT-X-ENDLIST), Media3 keeps window.isLive true, only window.isDynamic becomes false.
        val isLive = window.isLive && window.isDynamic
        when {
            isLive -> liveWindowUid = window.uid
            liveWindowUid != null && window.uid == liveWindowUid -> {
                liveWindowUid = null
                reloadCurrentMediaItem()
            }
        }
    }

    private fun reloadCurrentMediaItem() {
        val mediaItem = player.currentMediaItem ?: return
        val index = player.currentMediaItemIndex
        val positionMs = player.currentPosition
        DebugLogger.debug(TAG, "Live to VOD transition, reload ${mediaItem.mediaId} at $positionMs ms")

        // Replacing the MediaItem with Player.replaceMediaItem doesn't reload it, because PillarboxMediaSource.canUpdateMediaItem returns true.
        // So the new MediaItem is added after the current one, played at the same position, then the current one is removed.
        player.addMediaItem(index + 1, mediaItem.buildUpon().setMediaMetadata(MediaMetadata.EMPTY).build())
        player.seekTo(index + 1, positionMs)
        player.removeMediaItem(index)
    }

    private companion object {
        private const val TAG = "LiveToVodReloader"
    }
}
