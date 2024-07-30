/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Information about the operating system.
 *
 * @property name The name of the operating system.
 * @property version The version of the operating system.
 */
data class QoSOS(
    val name: String,
    val version: String,
)
