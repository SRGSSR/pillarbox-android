/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.NavigationDrawerScope
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesAll
import ch.srgssr.pillarbox.ui.extension.currentMediaItemIndexAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemsAsState

/**
 * Drawer used to display a player's playlist.
 *
 * @param player The currently active player.
 * @param modifier The [Modifier] to apply to this layout.
 */
@Composable
fun NavigationDrawerScope.PlaylistDrawer(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PlaylistRoutes.Main,
        modifier = modifier,
    ) {
        composable<PlaylistRoutes.Main> {
            val mediaItems by player.getCurrentMediaItemsAsState()
            val currentMediaItemIndex by player.currentMediaItemIndexAsState()

            PlaylistContent(
                mediaItems = mediaItems,
                currentMediaItemIndex = currentMediaItemIndex,
                onItemClick = { index, _ ->
                    player.seekToDefaultPosition(index)
                    player.play()
                },
                onEditClick = {
                    navController.navigate(PlaylistRoutes.Edit)
                },
            )
        }

        composable<PlaylistRoutes.Edit> {
            val mediaItems by player.getCurrentMediaItemsAsState()

            EditPlaylist(
                items = SamplesAll.playlist.items.map { it.toMediaItem() },
                playerItems = mediaItems,
                onAddClick = { items ->
                    player.addMediaItems(items)
                },
                onRemoveClick = { index ->
                    player.removeMediaItem(index)
                },
                onClearClick = { player.clearMediaItems() },
            )
        }
    }
}
