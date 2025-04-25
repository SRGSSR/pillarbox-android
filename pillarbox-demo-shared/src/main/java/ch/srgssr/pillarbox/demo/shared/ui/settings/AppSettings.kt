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
 * @property receiverApplicationId
 */
class AppSettings(
    val metricsOverlayEnabled: Boolean = false,
    val metricsOverlayTextSize: TextSize = TextSize.Medium,
    val metricsOverlayTextColor: TextColor = TextColor.Yellow,
    val receiverApplicationId: String = Default
) {

    /**
     * Receiver type from [receiverApplicationId].
     */
    val receiverType: ReceiverType = when (receiverApplicationId) {
        Letterbox -> ReceiverType.Letterbox
        Google -> ReceiverType.Google
        else -> ReceiverType.Custom
    }

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

    /**
     * Receiver type
     */
    @Suppress("UndocumentedPublicProperty")
    enum class ReceiverType {
        Letterbox,
        Google,
        Custom,
    }

    /**
     * Receiver application ID
     */
    companion object ReceiverId {
        /**
         * Letterbox receiver application ID
         */
        const val Letterbox = "1AC2931D"

        /**
         * Google receiver application ID
         */
        const val Google = "CC1AD845"

        /**
         * Default receiver application ID
         */
        const val Default = Letterbox
    }
}
