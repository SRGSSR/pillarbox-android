/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.playWhenReadyAsFlow
import ch.srgssr.pillarbox.ui.PlayerSurface
import ch.srgssr.pillarbox.ui.layout.AutoHideMode
import ch.srgssr.pillarbox.ui.layout.ToggleView
import ch.srgssr.pillarbox.ui.layout.rememberAutoHideState
import ch.srgssr.pillarbox.ui.playbackStateAsState

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
    val coroutineScope = rememberCoroutineScope()
    val playWhenReadyFlow = remember(player) {
        player.playWhenReadyAsFlow()
    }
    val playbackState = player.playbackStateAsState()
    val stateReady = playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING
    val playWhenReady = playWhenReadyFlow.collectAsState(initial = player.playWhenReady).value
    val delay: AutoHideMode = if (playWhenReady && stateReady) AutoHideMode.Delayed() else AutoHideMode.Disable
    val visibilityState = rememberAutoHideState(coroutineScope = coroutineScope, autoHideMode = delay)

    ToggleView(
        modifier = modifier,
        visibilityState = visibilityState,
        toggleableContent = {
            Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                TvPlaybackRow(player = player, state = visibilityState)
            }
        },
        content = {
            PlayerSurface(
                player = player,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}
