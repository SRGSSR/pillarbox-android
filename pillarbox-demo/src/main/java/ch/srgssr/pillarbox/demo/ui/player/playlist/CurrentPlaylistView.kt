/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.ui.currentMediaItemIndex
import ch.srgssr.pillarbox.ui.mediaItemCount
import ch.srgssr.pillarbox.ui.rememberPlayerState

/**
 * Current playlist view
 *
 * @param player Player to display current MediaItems
 * @param modifier Modifier
 * @param playerState PlayerState to use or create a new one.
 */
@Composable
fun CurrentPlaylistView(
    player: Player,
    modifier: Modifier = Modifier,
    playerState: PlayerState = rememberPlayerState(player = player),
) {
    val count = playerState.mediaItemCount()
    val currentMediaItemIndex = playerState.currentMediaItemIndex()
    LazyColumn(modifier = modifier) {
        items(count) {
            val mediaItem = player.getMediaItemAt(it)
            PlaylistItemView(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable(enabled = it != currentMediaItemIndex) {
                        player.seekTo(it, C.TIME_UNSET)
                        player.play()
                    },
                title = mediaItem.mediaMetadata.title.toString(),
                position = it,
                itemCount = count,
                currentPosition = currentMediaItemIndex,
                onRemoveItemIndex = player::removeMediaItem,
                onMoveItemIndex = player::moveMediaItem
            )
        }
    }
}
