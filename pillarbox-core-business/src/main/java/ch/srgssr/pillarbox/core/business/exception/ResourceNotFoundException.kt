/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import java.io.IOException

/**
 * This exception is thrown when a playable resource cannot be found for a given content.
 *
 * This typically occurs in scenarios where:
 *
 * - A [Chapter] does not have a playable resource.
 * - The [Chapter.listResource] is `null` or empty.
 *
 * @param message A descriptive message about the exception.
 */
class ResourceNotFoundException internal constructor(message: String) : IOException(message) {
    constructor() : this("Unable to find suitable resources")
}
