/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.ui

import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
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
import ch.srgssr.pillarbox.player.PillarboxPlayer

private const val TAG = "PillarboxSurface"

/**
 * Player view
 *
 * @param player
 * @param modifier
 * @param contentAlignment
 * @param crop to enable video crop
 * @param content composable matching the video size
 */
@Composable
fun PlayerView(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    crop: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    var playerSize by remember { mutableStateOf(player.videoSize) }
    Box(modifier = modifier, contentAlignment = contentAlignment) {
        player.videoScalingMode = if (crop) C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING else C.VIDEO_SCALING_MODE_DEFAULT
        val playerModifier = if (!crop) {
            val width: Int = playerSize.width
            val height: Int = playerSize.height
            val videoAspectRatio: Float = if (height == 0 || width == 0) 1.0f else width * playerSize.pixelWidthHeightRatio / height
            Modifier.aspectRatio(videoAspectRatio)
        } else {
            Modifier
        }
        Box(modifier = playerModifier) {
            PlayerSurface(player = player)
            content()
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                Log.d(TAG, "VideoSize changed to ${videoSize.width} x ${videoSize.height}")
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
        SurfaceView(it).apply {
            player.setVideoSurfaceView(this)
        }
    }, update = {
            Log.d(TAG, "update $player")
        })
}
