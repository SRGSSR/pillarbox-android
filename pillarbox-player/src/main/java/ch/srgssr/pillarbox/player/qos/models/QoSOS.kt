/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlinx.serialization.Serializable

/**
 * Information about the operating system.
 *
 * @property name The name of the operating system.
 * @property version The version of the operating system.
 */
@Serializable
data class QoSOS(
    val name: String,
    val version: String,
)
