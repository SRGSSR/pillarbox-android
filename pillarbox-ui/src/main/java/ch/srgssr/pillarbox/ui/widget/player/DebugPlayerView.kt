/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

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
