/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import java.io.IOException

/**
 * Data parsing exception
 *
 * @constructor
 *
 * @param message Message for the IOException, constructor used by PlaybackException to rebuild this exception.
 */
class DataParsingException internal constructor(message: String? = "Data parsing error") : IOException(message) {
    constructor(throwable: Throwable) : this(throwable.message)
}
