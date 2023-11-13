/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import java.io.IOException

/**
 * Resource not found exception is throw when :
 * - [Chapter] doesn't have a playable resource
 * - [Chapter.listResource] is empty or null
 */
class ResourceNotFoundException internal constructor(message: String) : IOException(message) {
    constructor() : this("Unable to find suitable resources")
}
