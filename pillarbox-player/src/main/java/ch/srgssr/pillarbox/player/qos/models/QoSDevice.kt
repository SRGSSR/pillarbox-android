/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Information about the device.
 *
 * @property id The unique identifier of the device.
 * @property model The model of the device.
 * @property type The type of device.
 */
data class QoSDevice(
    val id: String,
    val model: String,
    val type: DeviceType,
) {
    /**
     * The type of device.
     */
    enum class DeviceType {
        CAR,
        DESKTOP,
        PHONE,
        TABLET,
        TV,
        UNKNOWN,
    }
}
