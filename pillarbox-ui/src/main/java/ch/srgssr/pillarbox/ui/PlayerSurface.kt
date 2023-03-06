/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.player.computeAspectRatio

/**
 * Pillarbox player surface
 *
 * @param player The player to render in this SurfaceView
 * @param modifier The modifier to be applied to the layout.
 * @param scaleMode The scale mode to use.
 * @param contentAlignment The "letterboxing" content alignment inside the parent.
 * @param defaultAspectRatio The aspect ratio to use while video is loading or for audio content.
 * @param surfaceContent The Composable content to display on top of the Surface. May draw outside the view.
 */
@Composable
fun PlayerSurface(
    player: Player?,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    defaultAspectRatio: Float? = null,
    surfaceContent: @Composable (() -> Unit)? = null
) {
    val playerSize = rememberPlayerSize(player = player)
    val videoAspectRatio = playerSize.computeAspectRatio(unknownAspectRatioValue = defaultAspectRatio ?: 0.0f)
    AspectRatioBox(
        modifier = modifier,
        aspectRatio = videoAspectRatio,
        scaleMode = scaleMode,
        contentAlignment = contentAlignment
    ) {
        PlayerSurfaceView(player = player)
        surfaceContent?.invoke()
    }
}

@Composable
private fun rememberPlayerSize(player: Player?): VideoSize {
    var playerSize by remember(player) { mutableStateOf(player?.videoSize ?: VideoSize.UNKNOWN) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                playerSize = videoSize
            }
        }
        player?.addListener(listener)
        onDispose {
            player?.removeListener(listener)
        }
    }
    return playerSize
}
