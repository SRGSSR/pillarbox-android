/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a monitoring message.
 *
 * @property data The data associated with the message.
 * @property eventName The name of the event.
 * @property sessionId The unique identifier for the session during which the event occurred.
 * @property timestamp The timestamp of when the event occurred, in milliseconds.
 * @property version The version of the schema used for the [data] property.
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
     * Represents the name of the event that triggered a monitoring message.
     */
    @Suppress("UndocumentedPublicProperty")
    enum class EventName {
        ERROR,
        HEARTBEAT,
        START,
        STOP,
    }
}
