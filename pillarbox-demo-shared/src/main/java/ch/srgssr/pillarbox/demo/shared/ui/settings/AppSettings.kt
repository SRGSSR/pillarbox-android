/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.settings

import androidx.compose.ui.graphics.Color

/**
 * App settings
 *
 * @property metricsOverlayEnabled
 * @property metricsOverlayTextSize
 * @property metricsOverlayTextColor
 */
class AppSettings(
    val metricsOverlayEnabled: Boolean = false,
    val metricsOverlayTextSize: TextSize = TextSize.Medium,
    val metricsOverlayTextColor: TextColor = TextColor.Yellow,
) {

    /**
     * Text size
     */
    @Suppress("UndocumentedPublicProperty")
    enum class TextSize {
        Small,
        Medium,
        Large,
    }

    /**
     * Text color
     *
     * @property color the [Color].
     */
    @Suppress("UndocumentedPublicProperty")
    enum class TextColor(val color: Color) {
        Yellow(Color.Yellow),
        Red(Color.Red),
        Green(Color.Green),
        Blue(Color.Blue),
        White(Color.White)
    }
}
