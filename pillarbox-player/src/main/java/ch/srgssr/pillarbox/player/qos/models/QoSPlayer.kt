/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlinx.serialization.Serializable

/**
 * Information about the player.
 *
 * @property name The name of the player.
 * @property platform The platform of the player.
 * @property version The version of the player.
 */
@Serializable
data class QoSPlayer(
    val name: String,
    val platform: String,
    val version: String,
)
