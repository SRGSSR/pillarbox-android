/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Represents a QoS message.
 *
 * @property data The data associated with the message.
 * @property eventName The name of the event.
 * @property sessionId The session id.
 * @property timestamp The current timestamp.
 */
data class QoSMessage(
    val data: Any,
    val eventName: String,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
)
