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
 * This occurs when the [Chapter.listResource] returned by the integration layer is `null` or empty. The integration layer is responsible for
 * returning the resources playable by the device, based on the device capabilities sent with the request.
 *
 * @param message A descriptive message about the exception.
 */
class ResourceNotFoundException internal constructor(message: String) : IOException(message) {
    constructor() : this("Unable to find suitable resources")
}
