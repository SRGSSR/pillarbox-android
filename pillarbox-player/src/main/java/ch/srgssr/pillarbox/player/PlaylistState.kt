/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Playlist state keep track of the current Playlist of the player.
 *
 * @property player The Player to observe
 */
class PlaylistState(val player: Player) : PlayerDisposable {
    private val componentListener = ComponentListener()
    private val _currentMediaItems = MutableStateFlow(getCurrentMediaItems())
    private val _currentMediaItemIndex = MutableStateFlow(player.currentMediaItemIndex)
    private val _currentMediaItem = MutableStateFlow(player.currentMediaItem)

    /**
     * Current media item index [Player.getCurrentMediaItemIndex]
     */
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    /**
     * Current media item [Player.getCurrentMediaItem]
     */
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    /**
     * Current media items
     */
    val currentMediaItems: StateFlow<Array<MediaItem>> = _currentMediaItems.asStateFlow()

    init {
        player.addListener(componentListener)
    }

    private fun getCurrentMediaItems(): Array<MediaItem> {
        if (player.mediaItemCount == 0) {
            return emptyArray()
        }
        return Array(player.mediaItemCount) { index ->
            player.getMediaItemAt(index)
        }
    }

    override fun dispose() {
        player.removeListener(componentListener)
    }

    private inner class ComponentListener : Player.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                _currentMediaItems.value = getCurrentMediaItems()
                _currentMediaItemIndex.value = player.currentMediaItemIndex
                _currentMediaItem.value = player.currentMediaItem
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaItem.value = player.currentMediaItem
            _currentMediaItemIndex.value = player.currentMediaItemIndex
        }
    }
}
