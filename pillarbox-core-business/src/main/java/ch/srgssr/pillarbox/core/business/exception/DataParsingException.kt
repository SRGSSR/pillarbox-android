/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import java.io.IOException

/**
 * Represents an exception that occurs during data parsing.
 *
 * @param message A descriptive message about the exception.
 */
class DataParsingException internal constructor(message: String? = "Data parsing error") : IOException(message) {
    /**
     * Creates a new instance based on an existing [Throwable].
     *
     * @param throwable The underlying exception that caused the parsing error.
     */
    constructor(throwable: Throwable) : this(throwable.message)
}
