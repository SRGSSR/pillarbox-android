/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.extension.handleDPadKeyEvents
import ch.srgssr.pillarbox.ui.widget.maintainVisibleOnFocus
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
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
    Box(modifier = modifier) {
        PlayerSurface(
            player = player,
            modifier = Modifier
                .fillMaxSize()
                .handleDPadKeyEvents(onEnter = {
                    visibilityState.show()
                })
                .focusable(true)
        )
        AnimatedVisibility(
            visible = visibilityState.isVisible,
            enter = expandVertically { it },
            exit = shrinkVertically { it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .maintainVisibleOnFocus(delayedVisibilityState = visibilityState),
                contentAlignment = Alignment.Center
            ) {
                TvPlaybackRow(player = player, state = visibilityState)
            }
        }
        BackHandler(enabled = visibilityState.isVisible) {
            visibilityState.hide()
        }
    }
}
