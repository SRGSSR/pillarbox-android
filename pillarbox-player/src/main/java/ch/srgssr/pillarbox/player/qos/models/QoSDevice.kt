/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about the device.
 *
 * @property id The unique identifier of the device.
 * @property model The model of the device.
 * @property type The type of device.
 */
@Serializable
data class QoSDevice(
    val id: String,
    val model: String,
    val type: DeviceType?,
) {
    /**
     * The type of device.
     */
    enum class DeviceType {
        @SerialName("Car")
        CAR,

        @SerialName("Desktop")
        DESKTOP,

        @SerialName("Phone")
        PHONE,

        @SerialName("Tablet")
        TABLET,
        TV,
    }
}
