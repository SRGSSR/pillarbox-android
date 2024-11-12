/*
 * Copyright (c) SRG SSR. All rights reserved.
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
 * A Composable function that displays an ExoPlayer [PlayerControlView].
 *
 * @param player The [Player] instance to be controlled.
 * @param modifier The [Modifier] to apply to this layout.
 */
@Composable
fun ExoPlayerControlView(
    player: Player,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerControlView(context).apply {
                showTimeoutMs = -1
            }
        },
        update = {
            it.player = player
            it.showSubtitleButton = true
        },
        onRelease = {
            it.player = null
        },
        onReset = NoOpUpdate
    )
}
