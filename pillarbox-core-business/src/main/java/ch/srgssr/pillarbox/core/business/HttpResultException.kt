/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import io.ktor.client.plugins.ClientRequestException
import java.io.IOException

/**
 * Http result exception
 *
 * @constructor
 *
 * @param message Message for the IOException, constructor used by PlaybackException to rebuild this exception.
 */
class HttpResultException internal constructor(message: String) : IOException(message) {
    constructor(throwable: ClientRequestException) : this(
        "${throwable.response.status.description} (${throwable.response.status.value})"
    )
}
