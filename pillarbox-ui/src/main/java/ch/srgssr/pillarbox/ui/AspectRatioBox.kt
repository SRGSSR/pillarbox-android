/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

/**
 * Aspect ratio box
 *
 * @param modifier The modifier to be applied to the layout.
 * @param aspectRatio The aspect ratio to apply to the layout.
 * @param scaleMode The scale mode to use.
 * @param contentAlignment The "letterboxing" content alignment inside the parent.
 * @param content The composable children items
 * @receiver
 */
@Composable
fun AspectRatioBox(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0.0f,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit
) {
    val measurePolicy = contentViewMeasurePolicy(aspectRatio, scaleMode, contentAlignment)
    val internalModifier = if (scaleMode == ScaleMode.Crop) {
        modifier.clipToBounds()
    } else {
        modifier
    }
    Layout(measurePolicy = measurePolicy, content = content, modifier = internalModifier)
}

internal fun getContentConstraints(constraints: Constraints, aspectRatio: Float, scaleMode: ScaleMode): Constraints {
    if (aspectRatio == 0.0f) {
        return constraints
    }
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
        ScaleMode.Crop, ScaleMode.Zoom -> {
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
                    ScaleMode.Crop, ScaleMode.Zoom -> {
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
