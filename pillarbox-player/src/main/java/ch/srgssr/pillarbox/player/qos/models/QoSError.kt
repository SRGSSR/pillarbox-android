/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Represents a [Player][androidx.media3.common.Player] error to send to a QoS server.
 *
 * @property log The log associated with the error.
 * @property message The error message.
 * @property name The name of the error.
 * @property playerPosition The position of the player when the error occurred, in milliseconds, or `null` if not available.
 * @property severity The severity of the error, either [FATAL][Severity.FATAL] or [WARNING][Severity.WARNING].
 * @property url The last loaded url.
 */
data class QoSError(
    val log: String,
    val message: String,
    val name: String,
    val playerPosition: Long?,
    val severity: Severity,
    val url: String,
) {
    /**
     * Represents a [Player][androidx.media3.common.Player] error severity.
     */
    enum class Severity {
        FATAL,
        WARNING,
    }

    constructor(
        throwable: Throwable,
        playerPosition: Long?,
        severity: Severity,
        url: String,
    ) : this(
        log = throwable.stackTraceToString(),
        message = throwable.message.orEmpty(),
        name = throwable::class.simpleName.orEmpty(),
        playerPosition = playerPosition,
        severity = severity,
        url = url,
    )
}
