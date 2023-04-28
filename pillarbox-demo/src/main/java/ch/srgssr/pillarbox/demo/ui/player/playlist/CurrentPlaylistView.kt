/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlaylistState
import ch.srgssr.pillarbox.ui.currentMediaItemIndex
import ch.srgssr.pillarbox.ui.currentMediaItems
import ch.srgssr.pillarbox.ui.rememberPlaylistState

/**
 * Current playlist view
 *
 * @param player Player to display current MediaItems
 * @param modifier Modifier
 */
@Composable
fun CurrentPlaylistView(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val playlistState: PlaylistState = rememberPlaylistState(player = player)
    val currentMediaItemIndex = playlistState.currentMediaItemIndex()
    val mediaItems = playlistState.currentMediaItems()
    val count = mediaItems.size
    LazyColumn(modifier = modifier) {
        itemsIndexed(mediaItems) { index, mediaItem ->
            PlaylistItemView(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable(enabled = index != currentMediaItemIndex) {
                        player.seekTo(index, C.TIME_UNSET)
                        player.play()
                    },
                title = mediaItem.mediaMetadata.title.toString(),
                position = index,
                itemCount = count,
                currentPosition = currentMediaItemIndex,
                onRemoveItemIndex = player::removeMediaItem,
                onMoveItemIndex = player::moveMediaItem
            )
        }
    }
}
