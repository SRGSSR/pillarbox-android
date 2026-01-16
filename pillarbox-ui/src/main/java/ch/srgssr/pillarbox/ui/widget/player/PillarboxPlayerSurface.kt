/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A Composable function that displays a [Player].
 *
 *  Since minSDK = 24, [surfaceType] should be always [SurfaceType.Surface] or [SurfaceType.Spherical].
 *
 *  [Choosing surface type Media3 documentation](https://developer.android.com/media/media3/ui/surface)
 *
 * @param player The [Player] instance to use for playback.
 * @param modifier The [Modifier] to apply to the layout.
 * @param surfaceType The [SurfaceType] to use for rendering the video.
 */
@Composable
fun PillarboxPlayerSurface(
    player: Player?,
    modifier: Modifier = Modifier,
    surfaceType: SurfaceType = SurfaceType.Surface,
) {
    // Always leave PlayerSurface to be part of the Compose tree because it will be initialized in
    // the process. If this composable is guarded by some condition, it might never become visible
    // because the Player will not emit the relevant event, e.g. the first frame being ready.
    when (surfaceType) {
        SurfaceType.Surface -> PlayerSurface(player = player, modifier = modifier, surfaceType = SURFACE_TYPE_SURFACE_VIEW)
        SurfaceType.Texture -> PlayerSurface(player = player, modifier = modifier, surfaceType = SURFACE_TYPE_TEXTURE_VIEW)
        SurfaceType.Spherical -> PlayerSurfaceSphericalInternal(player = player, modifier = modifier)
    }
}

@Composable
private fun PlayerSurfaceSphericalInternal(player: Player?, modifier: Modifier) {
    var view by remember { mutableStateOf<SphericalGLSurfaceView?>(null) }
    AndroidView(
        modifier = modifier,
        factory = { SphericalGLSurfaceView(it) },
        onReset = {},
        onRelease = {
            it.onPause()
        },
        update = {
            view = it
            it.onResume()
        },
    )

    view?.let { view ->
        LaunchedEffect(view, player) {
            if (player != null) {
                view.attachedPlayer?.let { previousPlayer ->
                    if (previousPlayer != player && previousPlayer.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                        previousPlayer.clearVideoSurfaceView(view)
                    }
                }
                if (player.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                    player.setVideoSurfaceView(view)
                    view.attachedPlayer = player
                }
            } else {
                // Now that our player got null'd, we are not in a rush to get the old view from the
                // previous player. Instead, we schedule clearing of the view for later on the main thread,
                // since that player might have a new view attached to it in the meantime. This will avoid
                // unnecessarily creating a Surface placeholder.
                withContext(Dispatchers.Main) {
                    view.attachedPlayer?.let { previousPlayer ->
                        if (previousPlayer.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                            previousPlayer.clearVideoSurfaceView(view)
                        }
                        view.attachedPlayer = null
                    }
                }
            }
        }
    }
}

private var View.attachedPlayer: Player?
    get() = tag as? Player
    set(player) {
        tag = player
    }
