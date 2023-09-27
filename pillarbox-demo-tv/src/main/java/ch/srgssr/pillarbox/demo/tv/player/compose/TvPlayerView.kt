/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.PlayerSurface
import ch.srgssr.pillarbox.ui.ToggleView
import ch.srgssr.pillarbox.ui.rememberToggleState
import kotlin.time.Duration.Companion.seconds

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
    Box(modifier = modifier) {
        PlayerSurface(
            player = player,
            modifier = Modifier.fillMaxSize()
        ) {
            val toggleState = rememberToggleState(visible = true, duration = 4.seconds)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        toggleState.toggleVisible()
                    }
            ) {
                ToggleView(toggleState = toggleState) {
                    TvPlaybackRow(
                        player = player,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
    }
}
