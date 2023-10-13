/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.widget.ToggleableBox
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface2
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState

/**
 * Tv player view
 *
 * @param player
 * @param modifier
 */
@Composable
fun TvPlayerView(
    player: Player,
    modifier: Modifier = Modifier
) {
    val visibilityState = rememberDelayedVisibilityState(player = player, visible = true)
    ToggleableBox(
        modifier = modifier,
        visibilityState = visibilityState,
        toggleableContent = {
            Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                TvPlaybackRow(player = player, state = visibilityState)
            }
        },
        content = {
            PlayerSurface2(
                player = player,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}
