/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.exoplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
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
    AndroidView(
        modifier = modifier,
        factory = { PlayerControlView(it) },
        update = {
            it.player = player
            it.showTimeoutMs = -1
        }, onRelease = {
            it.player = null
        }, onReset = NoOpUpdate
    )
}
