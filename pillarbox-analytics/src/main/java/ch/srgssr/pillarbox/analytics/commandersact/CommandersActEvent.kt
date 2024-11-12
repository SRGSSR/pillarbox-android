/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import com.tagcommander.lib.serverside.events.TCCustomEvent

/**
 * Represents an event to be sent to Commanders Act.
 *
 * @property name The name of the event. Must not be blank.
 * @property labels A map of custom labels associated with the event. Defaults to an empty map. Please discuss the expected values for your
 * application with your measurement team.
 *
 * @throws IllegalArgumentException If [name] is blank.
 */
data class CommandersActEvent(
    val name: String,
    val labels: Map<String, String> = emptyMap(),
) {
    init {
        require(name.isNotBlank()) { "Name can't be blank!" }
    }

    /**
     * Converts this event into a `TCCustomEvent`.
     *
     * @return A `TCCustomEvent` instance populated with data from this instance.
     */
    fun toTCCustomEvent(): TCCustomEvent {
        val event = TCCustomEvent(name)
        for (customEntry in labels) {
            event.addAdditionalParameterIfNotBlank(customEntry.key, customEntry.value)
        }
        return event
    }
}
