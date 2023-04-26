/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlaylistState

/**
 * Current media item index [Player.getCurrentMediaItemIndex]
 */
@Composable
fun PlaylistState.currentMediaItemIndex() = currentMediaItemIndex.collectAsState().value

/**
 * Current media item [Player.getCurrentMediaItem]
 */
@Composable
fun PlaylistState.currentMediaItem() = currentMediaItem.collectAsState().value

/**
 * Current media items
 */
@Composable
fun PlaylistState.currentMediaItems() = currentMediaItems.collectAsState().value

/**
 * Remember playlist state
 *
 * @param player The player to create the [PlaylistState]
 */
@Composable
fun rememberPlaylistState(player: Player): PlaylistState {
    return rememberPlayerDisposable(player = player) { PlaylistState(it) }
}
