/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Represent current playlist items with current item
 *
 * @property currentMediaItem The current media item if any.
 * @property currentMediaItemIndex The current media item index or [C.INDEX_UNSET].
 * @property mediaItems The MediaItem array.
 */
data class PlaylistData(
    val currentMediaItem: MediaItem? = null,
    val currentMediaItemIndex: Int = C.INDEX_UNSET,
    val mediaItems: Array<MediaItem> = emptyArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaylistData

        if (currentMediaItemIndex != other.currentMediaItemIndex) return false
        if (currentMediaItem != other.currentMediaItem) return false
        if (!mediaItems.contentEquals(other.mediaItems)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentMediaItem?.hashCode() ?: 0
        result = 31 * result + currentMediaItemIndex
        result = 31 * result + mediaItems.contentHashCode()
        return result
    }
}

/**
 * Playlist state keep track of the current Playlist of the player.
 *
 * @property player The Player to observe
 */
class PlaylistState(val player: Player) {

    /**
     * Current playlist data
     */
    val currentPlaylistData: Flow<PlaylistData> = callbackFlow {
        var currentPlaylist =
            PlaylistData(
                currentMediaItem = player.currentMediaItem,
                currentMediaItemIndex = player.currentMediaItemIndex,
                getCurrentMediaItems()
            )
        val listener = object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                    currentPlaylist = PlaylistData(
                        currentMediaItem = player.currentMediaItem,
                        currentMediaItemIndex = player.currentMediaItemIndex,
                        getCurrentMediaItems()
                    )
                    trySend(currentPlaylist)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val newPlaylist =
                    currentPlaylist.copy(currentMediaItem = player.currentMediaItem, currentMediaItemIndex = player.currentMediaItemIndex)
                trySend(newPlaylist)
            }
        }
        trySend(currentPlaylist)
        player.addListener(listener)
        awaitClose {
            player.removeListener(listener)
        }
    }

    private fun getCurrentMediaItems(): Array<MediaItem> {
        if (player.mediaItemCount == 0) {
            return emptyArray()
        }
        return Array(player.mediaItemCount) { index ->
            player.getMediaItemAt(index)
        }
    }
}
