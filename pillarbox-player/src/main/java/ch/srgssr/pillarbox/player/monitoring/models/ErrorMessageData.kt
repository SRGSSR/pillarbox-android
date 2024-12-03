/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.extension.getUnixTimeMs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a [Player] error to send to a monitoring server.
 *
 * @property duration The duration of the media being played, in milliseconds.
 * @property log The log associated with the error.
 * @property message The error message.
 * @property name The name of the error.
 * @property position The playback position, in milliseconds, when the error occurred.
 * @property positionTimestamp The current player timestamp, as retrieved from the playlist.
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
    val url: String,
) : MessageData {
    constructor(
        throwable: Throwable,
        player: Player,
        url: String,
    ) : this(
        duration = player.duration,
        log = throwable.stackTraceToString(),
        message = throwable.message.orEmpty(),
        name = throwable::class.simpleName.orEmpty(),
        position = player.currentPosition,
        positionTimestamp = player.getUnixTimeMs().takeIf { it != C.TIME_UNSET },
        url = url,
    )
}
