/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.media3.common.PlaybackException
import java.io.IOException

/**
 * Represents an exception that occurs during an HTTP request when the server responds with an unsuccessful status code.
 *
 * @param message A descriptive message about the exception. Used by [PlaybackException] to rebuild this exception
 */
class HttpResultException internal constructor(message: String) : IOException(message) {
    /**
     * Creates a new instance based on an [IOException].
     *
     * @param throwable The underlying [IOException] that triggered this exception.
     */
    // TODO Use the proper exception type
    constructor(throwable: IOException) : this(throwable.message.orEmpty())
}
