/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player

/**
 * Pillarbox player surface
 *
 * @param player The player to render in this SurfaceView
 * @param modifier The modifier to be applied to the layout.
 * @param scaleMode The scale mode to use.
 * @param contentAlignment The "letterboxing" content alignment inside the parent.
 * @param defaultAspectRatio The aspect ratio to use while video is loading or for audio content.
 * @param surfaceContent The Composable content to display on top of the Surface.
 * The content matches the video surface when [scaleMode] is [ScaleMode.Fit]. By default it displays the subtitles.
 */
@Composable
fun PlayerSurface(
    player: Player,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    defaultAspectRatio: Float? = null,
    surfaceContent: @Composable (BoxScope.() -> Unit)? = {
        ExoPlayerSubtitleView(player = player)
    }
) {
    val videoAspectRatio = player.getAspectRatioAsState(
        defaultAspectRatio = defaultAspectRatio ?: 0.0f
    )
    val surfaceContentState = rememberUpdatedState(newValue = surfaceContent)
    Box(modifier = modifier) {
        AspectRatioBox(
            modifier = Modifier.fillMaxSize(),
            aspectRatio = videoAspectRatio,
            scaleMode = scaleMode,
            contentAlignment = contentAlignment
        ) {
            PlayerSurfaceView(player = player)
            if (scaleMode == ScaleMode.Fit) {
                surfaceContentState.value?.invoke(this)
            }
        }
        if (scaleMode != ScaleMode.Fit) {
            surfaceContentState.value?.invoke(this)
        }
    }
}
