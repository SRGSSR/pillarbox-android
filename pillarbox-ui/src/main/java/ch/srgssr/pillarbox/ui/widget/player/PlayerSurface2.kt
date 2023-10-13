/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.getAspectRatioAsState

/**
 * Pillarbox player surface
 *
 * @param player The player to render in this SurfaceView
 * @param modifier The modifier to be applied to the layout.
 * @param scaleMode The scale mode to use.
 * @param contentAlignment The "letterboxing" content alignment inside the parent.
 * @param defaultAspectRatio The aspect ratio to use while video is loading or for audio content.
 * @param displayDebugView When true displays debug information on top of the surface.
 * @param surfaceContent The Composable content to display on top of the Surface.
 */
@Composable
fun PlayerSurface2(
    player: Player,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    defaultAspectRatio: Float? = null,
    displayDebugView: Boolean = false,
    surfaceContent: @Composable (BoxScope.() -> Unit)? = { ExoPlayerSubtitleView(player = player) },
) {
    val videoAspectRatio = player.getAspectRatioAsState(
        defaultAspectRatio = defaultAspectRatio ?: 1.0f
    )
    BoxWithConstraints(
        contentAlignment = contentAlignment,
        modifier = modifier.then(Modifier.clipToBounds())
    ) {
        val width = constraints.minWidth.coerceAtLeast(constraints.maxWidth)
        val height = constraints.minHeight.coerceAtLeast(constraints.maxHeight).coerceAtLeast(1)
        val viewAspectRatio = width / height.toFloat()

        val videoSurfaceModifier = when (scaleMode) {
            ScaleMode.Fit -> {
                Modifier.aspectRatio(videoAspectRatio, viewAspectRatio > videoAspectRatio)
            }

            ScaleMode.Crop -> {
                Modifier
                    .fillMaxSize()
                    .aspectRatio(videoAspectRatio, viewAspectRatio <= videoAspectRatio)
            }

            ScaleMode.Fill -> {
                Modifier
            }
        }

        AndroidPlayerSurfaceView(modifier = videoSurfaceModifier, player = player)

        val overlayModifier = if (scaleMode != ScaleMode.Crop) {
            videoSurfaceModifier
        } else {
            Modifier.fillMaxSize()
        }
        surfaceContent?.let {
            Box(modifier = overlayModifier, content = it)
        }

        if (displayDebugView) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                BasicText(
                    text = "size: ${width}x$height",
                    color = { Color.Green }
                )
                BasicText(
                    text = "Aspect view: $viewAspectRatio video: $videoAspectRatio",
                    color = { Color.Green }
                )
            }
            DebugPlayerView(videoSurfaceModifier)
        }
    }
}

/**
 * Debug player view
 *
 * @param modifier The modifier to use to layout.
 */
@Composable
fun DebugPlayerView(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawLine(
            color = Color.Green,
            start = Offset.Zero,
            end = Offset(size.width, size.height),
            strokeWidth = 2f,
        )
        drawLine(
            color = Color.Green,
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2f,
        )
        drawRect(
            color = Color.Magenta,
            style = Stroke(width = 4f),
        )
    }
}
