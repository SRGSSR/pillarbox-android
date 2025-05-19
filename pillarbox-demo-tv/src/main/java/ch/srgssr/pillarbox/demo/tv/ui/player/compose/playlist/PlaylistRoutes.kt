/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist

import kotlinx.serialization.Serializable

/**
 * All the routes used in the player's playlist drawer.
 */
@Serializable
sealed interface PlaylistRoutes {
    /**
     * The route for the main screen of the playlist.
     */
    @Serializable
    data object Main : PlaylistRoutes

    /**
     * The route for the playlist edition.
     */
    @Serializable
    data object Edit : PlaylistRoutes
}
