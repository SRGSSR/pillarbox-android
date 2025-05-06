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
        Media3 -> ReceiverType.Media3
        Tv -> ReceiverType.Tv
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
        Media3,
        Tv,
        Custom;

        /**
         * Receiver application ID
         * @return null when [ReceiverType.Custom]
         */
        fun receiverId(): String? {
            return when (this) {
                Letterbox -> ReceiverId.Letterbox
                Google -> ReceiverId.Google
                Media3 -> ReceiverId.Media3
                Tv -> ReceiverId.Tv
                Custom -> null
            }
        }
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
         * Media3 default receiver application ID
         */
        const val Media3 = "A12D4273"

        const val Tv = "5718ACDA"

        /**
         * Default receiver application ID
         */
        const val Default = Letterbox
    }
}
