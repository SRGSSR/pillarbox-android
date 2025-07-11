/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import android.view.SurfaceView
import android.view.TextureView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView
import ch.srgssr.pillarbox.player.extension.containsImageTrack
import ch.srgssr.pillarbox.player.getCurrentTracksAsFlow
import ch.srgssr.pillarbox.player.tracks.videoTracks
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.getAspectRatioAsState

/**
 * A Composable function that displays a [Player].
 *
 * It supports different surface type, scaling modes, and allows for custom content to be laid on top of the [Player].
 *
 * @param player The [Player] instance to use for playback.
 * @param modifier The [Modifier] to apply to the layout.
 * @param scaleMode The scaling mode to use.
 * @param contentAlignment The "letterboxing" content alignment inside the parent.
 * @param defaultAspectRatio The default aspect ratio to use while the video is loading, or for audio content.
 * @param displayDebugView Whether to display a debug view showing video size and aspect ratio information. Defaults to false.
 * @param surfaceType The type of surface to use for rendering the video.
 * @param surfaceContent The content to display on top of the [Player].
 */
@Suppress("CyclomaticComplexMethod")
@Composable
fun PlayerSurface(
    player: Player,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    defaultAspectRatio: Float? = null,
    displayDebugView: Boolean = false,
    surfaceType: SurfaceType = SurfaceType.Surface,
    surfaceContent: @Composable (BoxScope.() -> Unit)? = { ExoPlayerSubtitleView(player = player) },
) {
    var lastKnownVideoAspectRatio by remember { mutableFloatStateOf(defaultAspectRatio ?: 1f) }
    val videoAspectRatio by player.getAspectRatioAsState(defaultAspectRatio = lastKnownVideoAspectRatio)
    val currentTracks by player.getCurrentTracksAsFlow().collectAsState(Tracks.EMPTY)
    val drawOnSurface by remember {
        derivedStateOf { currentTracks.isEmpty || currentTracks.videoTracks.isNotEmpty() || currentTracks.containsImageTrack() }
    }

    if (!drawOnSurface) {
        lastKnownVideoAspectRatio = 0f
    } else if (videoAspectRatio > 0f) {
        lastKnownVideoAspectRatio = videoAspectRatio
    }

    if (lastKnownVideoAspectRatio <= 0f) {
        Box(modifier)
        return
    }

    BoxWithConstraints(
        contentAlignment = contentAlignment,
        modifier = modifier.clipToBounds()
    ) {
        val width = constraints.minWidth.coerceAtLeast(constraints.maxWidth)
        val height = constraints.minHeight.coerceAtLeast(constraints.maxHeight).coerceAtLeast(1)
        val viewAspectRatio = width / height.toFloat()

        val videoSurfaceModifier = when (scaleMode) {
            ScaleMode.Fit -> {
                Modifier.aspectRatio(lastKnownVideoAspectRatio, viewAspectRatio > lastKnownVideoAspectRatio)
            }

            ScaleMode.Crop -> {
                Modifier
                    .fillMaxSize()
                    .aspectRatio(lastKnownVideoAspectRatio, viewAspectRatio <= lastKnownVideoAspectRatio)
            }

            ScaleMode.Fill -> {
                Modifier
            }
        }

        when (surfaceType) {
            SurfaceType.Surface -> AndroidPlayerSurfaceView(modifier = videoSurfaceModifier, player = player)
            SurfaceType.Texture -> AndroidPlayerTextureView(modifier = videoSurfaceModifier, player = player)
            SurfaceType.Spherical -> AndroidSphericalSurfaceView(modifier = videoSurfaceModifier, player = player)
        }

        surfaceContent?.let {
            val overlayModifier = if (scaleMode != ScaleMode.Crop) {
                videoSurfaceModifier
            } else {
                Modifier.fillMaxSize()
            }

            Box(modifier = overlayModifier, content = it)
        }

        if (displayDebugView) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                BasicText(
                    text = "Size: ${width}x$height",
                    color = { Color.Green }
                )
                BasicText(
                    text = "Aspect ratio view: $viewAspectRatio, video: $lastKnownVideoAspectRatio",
                    color = { Color.Green }
                )
            }
            DebugPlayerView(videoSurfaceModifier)
        }
    }
}

/**
 * Represents the type of surface used for video rendering.
 */
enum class SurfaceType {
    /**
     * Renders the video into a [SurfaceView].
     *
     * This is the most optimized option, and it supports DRM content.
     */
    Surface,

    /**
     * Renders the video into a [TextureView].
     *
     * This option may be interesting when dealing with animation, and the [SurfaceType.Surface] option doesn't work as expected. However, it does
     * not support DRM content.
     */
    Texture,

    /**
     * Renders the video into a [SphericalGLSurfaceView].
     *
     * This is suited for 360° video content. However, it does not support DRM content.
     */
    Spherical,
}

/**
 * Debug player view
 *
 * @param modifier The modifier to use to layout.
 */
@Composable
private fun DebugPlayerView(modifier: Modifier) {
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
