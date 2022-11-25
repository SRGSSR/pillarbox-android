/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations.adaptive

import android.content.Context
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.ui.AspectRatioFrameLayout
import ch.srg.pillarbox.ui.PlayerSurface
import ch.srg.pillarbox.ui.PlayerView
import ch.srg.pillarbox.ui.ResizeMode
import ch.srg.pillarbox.ui.computeAspectRatio
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Adaptive view demo to play with player size and resize mode
 */
@Composable
fun AdaptiveViewDemo() {
    val viewModel: AdaptiveViewModel = viewModel()
    AdaptiveView(modifier = Modifier.fillMaxSize(), player = viewModel.player)
}

@Composable
private fun AdaptiveView(modifier: Modifier = Modifier, player: PillarboxPlayer) {
    var resizeMode by remember {
        mutableStateOf(ResizeMode.Fit)
    }
    var widthPercent by remember {
        mutableStateOf(1f)
    }
    var heightPercent by remember {
        mutableStateOf(1f)
    }
    BoxWithConstraints(modifier = modifier.padding(12.dp)) {
        Box(
            modifier = Modifier
                .size(maxWidth * widthPercent, maxHeight * heightPercent)
                .background(color = Color.Black),
            contentAlignment = Alignment.Center
        ) {
            PlayerView(
                modifier = Modifier.matchParentSize(),
                resizeMode = resizeMode,
                player = player
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.surface.copy(0.5f))
                .align(Alignment.BottomStart)
        ) {
            SliderWithLabel(label = "W: ", value = widthPercent, onValueChange = { widthPercent = it })
            SliderWithLabel(label = "H :", value = heightPercent, onValueChange = { heightPercent = it })
            Row {
                for (mode in ResizeMode.values()) {
                    RadioButtonWithLabel(label = mode.name, selected = mode == resizeMode) {
                        resizeMode = mode
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioButtonWithLabel(modifier: Modifier = Modifier, label: String, selected: Boolean, onClick: (() -> Unit)) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.caption)
    }
}

@Composable
private fun SliderWithLabel(modifier: Modifier = Modifier, label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(modifier) {
        Text(text = label)
        Slider(value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun PlayerDemo2(player: PillarboxPlayer, modifier: Modifier = Modifier, resizeMode: ResizeMode = ResizeMode.Zoom) {
    var playerSize by remember { mutableStateOf(player.videoSize) }
    val aspectRatio = playerSize.computeAspectRatio(1f)
    BoxWithConstraints(modifier = modifier) {
        PlayerSurface1(
            player = player,
            modifier = Modifier,
            aspectRatio = aspectRatio,
            resizeMode = ResizeMode.Zoom
        )
        Text(text = "Size($maxWidth x $maxHeight", color = Color.Red)
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
fun PlayerSurface1(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1.0f,
    resizeMode: ResizeMode
) {
    BoxWithConstraints(modifier = modifier) {
        var width = maxWidth
        var height = maxHeight
        val viewAspectRatio = width / height.coerceAtLeast(1.dp)
        val aspectDeformation: Float = aspectRatio / viewAspectRatio - 1

        when (resizeMode) {
            ResizeMode.FixedWidth -> height = (width / aspectRatio)
            ResizeMode.FixedHeight -> width = (height * aspectRatio)
            ResizeMode.Zoom -> if (aspectDeformation > 0) {
                width = (height * aspectRatio)
            } else {
                height = (width / aspectRatio)
            }
            ResizeMode.Fit -> if (aspectDeformation > 0) {
                height = (width / aspectRatio)
            } else {
                width = (height * aspectRatio)
            }
            ResizeMode.Fill -> {}
        }
        PlayerSurface(player = player, modifier = Modifier.size(width, height))
    }
}

/**
 * Player surface2
 *
 * Try to use AspectRatioLayout from Exoplayer with a SurfaceView
 *
 * Doesn't work as expected
 */
@Composable
fun PlayerSurface2(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1.0f,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
) {
    AndroidView(modifier = modifier, factory = {
        PlayerViewLayout(it).apply {
            this.player = player
        }
    }, update = { view ->
            view.player = player
            view.setAspectRatio(aspectRatio)
            if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM || resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            } else {
                player.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
            }
            view.resizeMode = resizeMode
        })
}

internal class PlayerViewLayout(context: Context) : FrameLayout(context) {
    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.clearVideoSurfaceView(surfaceView)
                value?.setVideoSurfaceView(surfaceView)
            }
            field = value
        }

    var resizeMode: @AspectRatioFrameLayout.ResizeMode Int
        get() {
            return aspectRatioLayout.resizeMode
        }
        set(value) {
            aspectRatioLayout.resizeMode = value
        }

    private val aspectRatioLayout = AspectRatioFrameLayout(context)
    private val surfaceView = SurfaceView(context)

    init {
        addView(aspectRatioLayout)
        aspectRatioLayout.addView(surfaceView)
    }

    fun setAspectRatio(aspectRatio: Float) {
        aspectRatioLayout.setAspectRatio(aspectRatio)
    }
}
