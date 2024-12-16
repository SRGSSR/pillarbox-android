/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import androidx.media3.common.PlaybackException
import java.io.IOException

/**
 * Represents an exception that occurs during an HTTP request when the server responds with an unsuccessful status code.
 *
 * @param message A descriptive message about the exception. Used by [PlaybackException] to rebuild this exception
 */
class HttpResultException private constructor(message: String) : IOException(message) {
    /**
     * Creates a new instance based on a HTTP status code and message.
     *
     * @param statusCode The HTTP code received by the server.
     * @param statusMessage The message received by the server.
     */
    constructor(statusCode: Int, statusMessage: String) : this("$statusMessage ($statusCode)")
}
