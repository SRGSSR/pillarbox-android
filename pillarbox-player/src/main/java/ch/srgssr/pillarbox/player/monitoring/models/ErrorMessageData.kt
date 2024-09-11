/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.extension.getPositionTimestamp
import ch.srgssr.pillarbox.player.monitoring.models.ErrorMessageData.Severity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a [Player][androidx.media3.common.Player] error to send to a QoS server.
 *
 * @property duration The duration of the media being player, in milliseconds.
 * @property log The log associated with the error.
 * @property message The error message.
 * @property name The name of the error.
 * @property position The position of the player when the error occurred, in milliseconds, or `null` if not available.
 * @property positionTimestamp The current player timestamp, as retrieved from the playlist.
 * @property severity The severity of the error, either [FATAL][Severity.FATAL] or [WARNING][Severity.WARNING].
 * @property url The last loaded url.
 */
@Serializable
data class ErrorMessageData(
    val duration: Long?,
    val log: String,
    val message: String,
    val name: String,
    val position: Long?,
    @SerialName("position_timestamp") val positionTimestamp: Long?,
    val severity: Severity,
    val url: String,
) : MessageData {
    /**
     * Represents a [Player][androidx.media3.common.Player] error severity.
     */
    enum class Severity {
        FATAL,
        WARNING,
    }

    constructor(
        throwable: Throwable,
        severity: Severity,
        player: Player,
        url: String,
    ) : this(
        duration = player.duration,
        log = throwable.stackTraceToString(),
        message = throwable.message.orEmpty(),
        name = throwable::class.simpleName.orEmpty(),
        position = player.currentPosition,
        positionTimestamp = player.getPositionTimestamp(),
        severity = severity,
        url = url,
    )
}
