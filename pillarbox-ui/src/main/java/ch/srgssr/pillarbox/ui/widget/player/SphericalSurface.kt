/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState

/**
 * Render [player] content on a [SphericalGLSurfaceView]
 *
 * @param player The player to render.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun SphericalSurface(player: Player, modifier: Modifier = Modifier) {
    val playerError by player.playerErrorAsState()
    if (playerError != null) {
        return
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SphericalGLSurfaceView(context)
        }, update = { view ->
            player.setVideoSurfaceView(view)
            view.onResume()
        }, onRelease = { view ->
            player.setVideoSurfaceView(null)
            view.onPause()
        }, onReset = {
            // onRested is called before update when composable is reuse with different context.
            player.setVideoSurface(null)
        }
    )
}
