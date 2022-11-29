/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.C.VideoScalingMode
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import kotlin.math.roundToInt

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
 * @param defaultAspectRatio aspect ratio to use when not a video or player hasn't retrieve it
 * @param scaleMode how content is scaling
 * @param contentAlignment alignment inside the view
 * @param content that match the video surface view
 */
@Composable
fun PlayerView(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    defaultAspectRatio: Float = 1.0f,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit = {}
) {
    var playerSize by remember { mutableStateOf(player.videoSize) }
    val videoScalingMode = if (scaleMode == ScaleMode.Crop) C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING else C.VIDEO_SCALING_MODE_DEFAULT
    val aspectRatio = playerSize.computeAspectRatio(defaultAspectRatio)
    ScaleAspectRatioBox(modifier = modifier, scaleMode = scaleMode, aspectRatio = aspectRatio, contentAlignment = contentAlignment) {
        // Surface view
        PlayerVideoSurface(
            modifier = Modifier
                .background(color = Color.Black),
            player = player, videoScalingMode = videoScalingMode
        )
        content.invoke()
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

@Composable
internal fun ScaleAspectRatioBox(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1.0f,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit
) {
    val measurePolicy = contentViewMeasurePolicy(aspectRatio, scaleMode, contentAlignment)
    // Add  clipToBounds to clip view in crop mode
    val m = if (scaleMode == ScaleMode.Crop) {
        modifier.clipToBounds()
    } else {
        modifier
    }
    Layout(measurePolicy = measurePolicy, content = content, modifier = m)
}

internal fun getContentConstraints(constraints: Constraints, aspectRatio: Float, scaleMode: ScaleMode): Constraints {
    val width = constraints.minWidth.coerceAtLeast(constraints.maxWidth)
    val height = constraints.minHeight.coerceAtLeast(constraints.maxHeight)
    val viewAspectRatio = width / height.coerceAtLeast(1)
    val aspectDeformation: Float = aspectRatio / viewAspectRatio - 1
    return when (scaleMode) {
        ScaleMode.Fit -> {
            var contentWidth = width
            var contentHeight = height
            if (aspectDeformation > 0) {
                contentHeight = (width / aspectRatio).roundToInt()
            } else {
                contentWidth = (height * aspectRatio).roundToInt()
            }
            Constraints.fixed(contentWidth, contentHeight)
        }
        ScaleMode.Crop -> {
            var contentWidth = width
            var contentHeight = height
            if (aspectDeformation > 0) {
                contentWidth = (height * aspectRatio).roundToInt()
            } else {
                contentHeight = (width / aspectRatio).roundToInt()
            }
            Constraints.fixed(contentWidth, contentHeight)
        }
        else -> {
            constraints
        }
    }
}

internal fun contentViewMeasurePolicy(aspectRatio: Float, scaleMode: ScaleMode, contentAlignment: Alignment) =
    MeasurePolicy { measurables, constraints ->
        val contentConstraints = getContentConstraints(constraints, aspectRatio, scaleMode)
        val placeables = measurables.map { measurable -> measurable.measure(contentConstraints) }
        val size = if (!(constraints.hasFixedWidth && constraints.hasFixedHeight)) {
            var maxWidth = constraints.minWidth
            var maxHeight = constraints.minHeight
            for (placable in placeables) {
                maxWidth = maxWidth.coerceAtLeast(placable.measuredWidth)
                maxHeight = maxHeight.coerceAtLeast(placable.measuredHeight)
            }
            IntSize(maxWidth, maxHeight)
        } else {
            IntSize(constraints.maxWidth, constraints.maxHeight)
        }

        layout(size.width, size.height) {
            for (placeable in placeables) {
                var x = 0
                var y = 0
                when (scaleMode) {
                    ScaleMode.Crop -> {
                        x = -(placeable.width / 2f).roundToInt() + size.width / 2
                        y = -(placeable.height / 2f).roundToInt() + size.height / 2
                    }
                    ScaleMode.Fit -> {
                        val offset = contentAlignment.align(IntSize(placeable.width, placeable.height), size, LayoutDirection.Ltr)
                        x = offset.x
                        y = offset.y
                    }
                    else -> {}
                }
                placeable.place(x, y, 0f)
            }
        }
    }

/**
 * SurfaceView to display [player] video output
 *
 * @param player
 * @param modifier
 * @param videoScalingMode to optimize crop if needed
 */
@Composable
fun PlayerVideoSurface(player: ExoPlayer, modifier: Modifier = Modifier, videoScalingMode: @VideoScalingMode Int = C.VIDEO_SCALING_MODE_DEFAULT) {
    AndroidView(modifier = modifier, factory = {
        Log.d(TAG, "Create SurfaceView")
        PlayerSurfaceView(it).apply {
            this.videoScalingMode = videoScalingMode
            this.player = player
        }
    }, update = { view ->
            Log.d(TAG, "update $player")
            view.videoScalingMode = videoScalingMode
            view.player = player
        })
}

@Preview
@Composable
@Suppress("MagicNumber")
private fun PreviewContentView() {
    val aspectRatio = 16 / 9f
    val scaleType = ScaleMode.Fit
    val alignment = Alignment.Center
    ScaleAspectRatioBox(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.Black),
        scaleMode = scaleType,
        aspectRatio = aspectRatio,
        contentAlignment = alignment
    ) {
        Canvas(
            modifier = Modifier
                .background(color = Color.Red)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            drawLine(
                start = Offset(x = canvasWidth, y = 0f),
                end = Offset(x = 0f, y = canvasHeight),
                color = Color.Black,
                strokeWidth = 5F
            )
            drawLine(
                end = Offset(x = canvasWidth, y = canvasHeight),
                start = Offset(x = 0f, y = 0f),
                color = Color.Black,
                strokeWidth = 5F
            )
        }
        Box(modifier = Modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Player content",
                style = MaterialTheme.typography.h2,
                textAlign = TextAlign.Center
            )
        }
    }
}
