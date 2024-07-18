/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.metrics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

/**
 * Metrics overlay options
 *
 * @property color The [Color] for the text overlay.
 * @property size The [TextUnit] for the text overlay.
 */
data class MetricsOverlayOptions(
    val color: Color = Color.Yellow,
    val size: TextUnit = TextUnit.Unspecified,
)
