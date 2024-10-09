/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView

/**
 * Render the [player] content on a [SphericalGLSurfaceView].
 *
 * @param player The player to render.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
internal fun AndroidSphericalSurfaceView(player: Player, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SphericalGLSurfaceView(context)
        },
        update = { view ->
            player.setVideoSurfaceView(view)
            view.onResume()
        },
        onRelease = { view ->
            player.setVideoSurfaceView(null)
            view.onPause()
        },
        onReset = {
            // onReset is called before `update` when the composable is reused with a different context.
            player.setVideoSurface(null)
        }
    )
}
