/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a QoS message.
 *
 * @property data The data associated with the message.
 * @property eventName The name of the event.
 * @property sessionId The session id.
 * @property timestamp The current timestamp.
 * @property version The version of the schema used in [data].
 */
@Serializable
data class Message(
    val data: MessageData,
    @SerialName("event_name") val eventName: EventName,
    @SerialName("session_id") val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val version: Int = 1,
) {
    /**
     * The name of the event that triggered this QoS message.
     */
    @Suppress("UndocumentedPublicProperty")
    enum class EventName {
        ERROR,
        HEARTBEAT,
        START,
        STOP,
    }
}
