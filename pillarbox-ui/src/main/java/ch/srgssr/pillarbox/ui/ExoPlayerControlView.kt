/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerControlView

/**
 * Composable basic version of [PlayerControlView] from Media3 (Exoplayer)
 *
 * @param player The player to bind to the controls.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun ExoPlayerControlView(player: Player, modifier: Modifier = Modifier) {
    val playerControlView = rememberPlayerControlView(player)
    AndroidView(
        modifier = modifier,
        factory = { playerControlView },
        update = {
            it.player = player
            it.showTimeoutMs = -1
        }
    )
}

@Composable
private fun rememberPlayerControlView(player: Player): PlayerControlView {
    val context = LocalContext.current
    val playerControlView = remember(player) {
        PlayerControlView(context)
    }
    DisposableEffect(playerControlView) {
        onDispose {
            playerControlView.player = null
        }
    }
    return playerControlView
}
