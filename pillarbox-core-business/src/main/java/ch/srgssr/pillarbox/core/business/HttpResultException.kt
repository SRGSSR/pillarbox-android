/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.media3.common.PlaybackException
import io.ktor.client.plugins.ClientRequestException
import java.io.IOException

/**
 * Represents an exception that occurs during an HTTP request when the server responds with an unsuccessful status code.
 *
 * @param message A descriptive message about the exception. Used by [PlaybackException] to rebuild this exception
 */
class HttpResultException internal constructor(message: String) : IOException(message) {
    /**
     * Creates a new instance based on a [ClientRequestException].
     *
     * @param throwable The underlying [ClientRequestException] that triggered this exception.
     */
    constructor(throwable: ClientRequestException) : this(
        "${throwable.response.status.description} (${throwable.response.status.value})"
    )
}
