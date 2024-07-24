/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * App settings
 *
 * @property metricsOverlayEnabled
 * @property metricsOverlayTextSize
 * @property metricsOverlayTextColor
 */
class AppSettings(
    val metricsOverlayEnabled: Boolean = false,
    val metricsOverlayTextSize: TextSize = TextSize.MEDIUM,
    val metricsOverlayTextColor: TextColor = TextColor.Yellow,
) {

    /**
     * Text size
     *
     * @property size the [TextUnit].
     */
    enum class TextSize(val size: TextUnit) {
        SMALL(8.sp),
        MEDIUM(12.sp),
        LARGE(18.sp),
    }

    /**
     * Text color
     *
     * @property color the [Color].
     */
    enum class TextColor(val color: Color) {
        Yellow(Color.Yellow),
        Red(Color.Red),
        Green(Color.Green),
        Blue(Color.Blue),
        White(Color.White)
    }
}
