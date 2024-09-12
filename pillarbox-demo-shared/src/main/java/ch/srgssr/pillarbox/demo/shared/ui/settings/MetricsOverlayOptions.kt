/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Metrics overlay options
 *
 * @property textColor The [Color] for the text overlay.
 * @property textStyle The [TextStyle] for the text overlay.
 */
data class MetricsOverlayOptions(
    val textColor: Color = Color.Yellow,
    val textStyle: TextStyle = TextStyle.Default,
)
