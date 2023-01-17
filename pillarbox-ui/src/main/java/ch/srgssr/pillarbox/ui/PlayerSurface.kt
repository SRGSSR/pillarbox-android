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

/**
 * Render [player] content on a [PlayerSurfaceView]
 *
 * @param player The player to render on the SurfaceView.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlayerSurface(player: Player?, modifier: Modifier = Modifier) {
    val playerSurfaceView = rememberPlayerView()
    AndroidView(
        modifier = modifier,
        factory = { playerSurfaceView }, update = { view ->
            view.player = player
        }
    )
}

/**
 * Remember player view
 *
 * Create a [PlayerSurfaceView] that is Lifecyle aware.
 * OnDispose remove player from the view.
 *
 * @return the [PlayerSurfaceView]
 */
@Composable
private fun rememberPlayerView(): PlayerSurfaceView {
    val context = LocalContext.current
    val playerView = remember {
        PlayerSurfaceView(context)
    }

    DisposableEffect(playerView) {
        onDispose {
            playerView.player = null
        }
    }
    return playerView
}
