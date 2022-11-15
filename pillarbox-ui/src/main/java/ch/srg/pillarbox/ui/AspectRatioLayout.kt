/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints

/**
 * Composable conversion of [AspectRatioFrameLayout]
 *
 * @param modifier
 * @param aspectRatio > 0
 * @param resizeMode
 * @param content
 */
@Composable
fun AspectRatioBox(
    modifier: Modifier = Modifier,
    aspectRatio: Float,
    resizeMode: ResizeMode = ResizeMode.Fit,
    content: @Composable () -> Unit
) {
    val measurePolicy = aspectRatioBoxConstrainPolicy(aspectRatio, resizeMode)
    Layout(measurePolicy = measurePolicy, content = content, modifier = modifier)
}

internal fun aspectRatioBoxConstrainPolicy(aspectRatio: Float, scale: ResizeMode) = MeasurePolicy { measurables, constraints ->
    var width = constraints.minWidth.coerceAtLeast(constraints.maxWidth)
    var height = constraints.minHeight.coerceAtLeast(constraints.maxHeight)
    val viewAspectRatio = width / height.coerceAtLeast(1)
    val aspectDeformation: Float = aspectRatio / viewAspectRatio - 1

    when (scale) {
        ResizeMode.FixedWidth -> height = (width / aspectRatio).toInt()
        ResizeMode.FixedHeight -> width = (height * aspectRatio).toInt()
        ResizeMode.Zoom -> if (aspectDeformation > 0) {
            width = (height * aspectRatio).toInt()
        } else {
            height = (width / aspectRatio).toInt()
        }
        ResizeMode.Fit -> if (aspectDeformation > 0) {
            height = (width / aspectRatio).toInt()
        } else {
            width = (height * aspectRatio).toInt()
        }
        ResizeMode.Fill -> {}
    }

    val childConstraints = Constraints.fixed(width, height)
    val placeables = measurables.map { measurable -> measurable.measure(childConstraints) }
    layout(width, height) {
        for (placeable in placeables) {
            placeable.placeRelative(0, 0, 0f)
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun AspectRatioBoxPreview() {
    val aspectRatio = 4 / 3f
    val resizeMode = ResizeMode.Fill
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AspectRatioBox(
            modifier = Modifier
                .background(color = Color.Blue),
            aspectRatio = aspectRatio, resizeMode = resizeMode
        ) {
            Box(modifier = Modifier.background(Color.Red)) // modifier.scale = Crop
            Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier.wrapContentSize(), // modifier.scale = FitCenter
                    horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {}) {
                        Text(text = "Button 0")
                    }
                    Button(onClick = {}) {
                        Text(text = "Button 1")
                    }
                    Button(onClick = {}) {
                        Text(text = "Button 2")
                    }
                }
            }
        }
    }
}

/**
 * Resize mode
 */
enum class ResizeMode {
    Fit,
    FixedWidth,
    FixedHeight,
    Fill,
    Zoom
}
