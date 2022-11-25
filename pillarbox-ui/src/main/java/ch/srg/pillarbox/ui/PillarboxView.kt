/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.ui

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.player.PillarboxPlayer

private const val TAG = "PillarboxSurface"

/**
 * Compute aspect ratio, return [unknownAspectRatioValue] if aspect ratio can't be computed.
 *
 * @param unknownAspectRatioValue
 */
fun VideoSize.computeAspectRatio(unknownAspectRatioValue: Float): Float {
    return if (height == 0 || width == 0) unknownAspectRatioValue else width * this.pixelWidthHeightRatio / height
}

/**
 * Player view
 *
 * @param player
 * @param modifier
 * @param contentAlignment
 * @param resizeMode
 * @param defaultAspectRatio
 * @param content
 * @receiver
 */
@Composable
fun PlayerView(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    resizeMode: ResizeMode = ResizeMode.Fit,
    defaultAspectRatio: Float = 1.0f,
    content: @Composable () -> Unit = {}
) {
    var playerSize by remember { mutableStateOf(player.videoSize) }
    player.videoScalingMode = if (resizeMode == ResizeMode.Zoom) C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING else C.VIDEO_SCALING_MODE_DEFAULT
    Box(modifier = modifier, contentAlignment = contentAlignment) {
        AspectRatioBox(modifier = Modifier, aspectRatio = playerSize.computeAspectRatio(defaultAspectRatio), resizeMode = resizeMode) {
            PlayerSurface(player = player)
            content.invoke()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                playerSize = videoSize
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }
}

/**
 * Player video view using a [SurfaceView]
 *
 * @param player
 * @param modifier
 */
@Composable
fun PlayerSurface(player: Player, modifier: Modifier = Modifier) {
    AndroidView(modifier = modifier, factory = {
        Log.d(TAG, "Create SurfaceView")
        ch.srg.pillarbox.ui.PlayerView(it).apply {
            this.player = player
        }
    }, update = { view ->
            Log.d(TAG, "update $player")
            view.player = player
        })
}

/**
 * Very simple [PlayerView] Composable function
 *
 * @param player
 * @param modifier
 */
@Composable
fun ExoPlayerView(player: Player, modifier: Modifier = Modifier) {
    AndroidView(modifier = modifier, factory = {
        PlayerView(it).apply {
            this.player = player
            this.useController = true
        }
    }, update = { playerView ->
            playerView.player = player
        })
}

internal class PlayerView(context: Context) : SurfaceView(context) {
    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.clearVideoSurfaceView(this)
                value?.setVideoSurfaceView(this)
            }
            field = value
        }
}
