/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

/**
 * Represents the location from which requests to the integration layer are made.
 */
enum class IlLocation {
    /**
     * Represents Switzerland.
     */
    CH,

    /**
     * Represents the rest of the world.
     */
    WW;

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Retrieves an [IlLocation] associated with the given [name].
         *
         * @param name The name to search for.
         * @return The [IlLocation] associated with the name, or `null` if not found.
         */
        fun fromName(name: String): IlLocation? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}
