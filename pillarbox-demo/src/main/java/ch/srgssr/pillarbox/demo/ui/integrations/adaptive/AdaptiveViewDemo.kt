/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations.adaptive

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srg.pillarbox.ui.PlayerView
import ch.srg.pillarbox.ui.ScaleMode
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
@Suppress("MagicNumber")
private fun AdaptiveView(modifier: Modifier = Modifier, player: PillarboxPlayer) {
    var resizeMode by remember {
        mutableStateOf(ScaleMode.Fit)
    }
    var widthPercent by remember {
        mutableStateOf(1f)
    }
    var heightPercent by remember {
        mutableStateOf(1f)
    }
    BoxWithConstraints(modifier = modifier.padding(12.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(maxWidth * widthPercent, maxHeight * heightPercent)
                .background(color = Color.Black),
            contentAlignment = Alignment.Center
        ) {
            PlayerView(
                modifier = Modifier.matchParentSize(),
                scaleMode = resizeMode,
                player = player
            ) {
                Mire()
            }
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
                for (mode in ScaleMode.values()) {
                    RadioButtonWithLabel(label = mode.name, selected = mode == resizeMode) {
                        resizeMode = mode
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("MagicNumber")
private fun Mire(modifier: Modifier = Modifier) {
    val color = Color.Green
    Canvas(
        modifier = modifier
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        drawLine(
            start = Offset(x = canvasWidth, y = 0f),
            end = Offset(x = 0f, y = canvasHeight),
            color = color,
            strokeWidth = 5F
        )
        drawLine(
            end = Offset(x = canvasWidth, y = canvasHeight),
            start = Offset(x = 0f, y = 0f),
            color = color,
            strokeWidth = 5F
        )
        drawCircle(color, 10F, Offset(canvasWidth / 2, canvasHeight / 2))
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
