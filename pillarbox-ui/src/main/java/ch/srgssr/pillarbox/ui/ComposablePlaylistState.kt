/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlaylistData
import ch.srgssr.pillarbox.player.PlaylistState

/**
 * Current playlist data
 */
@Composable
fun PlaylistState.currentPlaylistData() = currentPlaylistData.collectAsState(PlaylistData()).value

/**
 * Remember playlist state
 *
 * @param player The player to create the [PlaylistState]
 */
@Composable
fun rememberPlaylistState(player: Player): PlaylistState {
    return remember(player) {
        PlaylistState(player)
    }
}
