/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.commandersact.TCEventExtensions.addAdditionalParameterIfNotBlank
import com.tagcommander.lib.serverside.events.TCCustomEvent

/**
 * Analytics Event
 *
 * All properties are loosely defined, please discuss expected values for your application with your measurement team)
 *
 * @property name The mandatory event name.
 * @property labels The custom labels to send with. Blank value are not send.
 */
data class CommandersActEvent(
    val name: String,
    val labels: Map<String, String> = emptyMap(),
) {
    init {
        require(name.isNotBlank()) { "Name can't be blank!" }
    }

    /**
     * Convert into TagCommander event.
     *
     * @return [TCCustomEvent]
     */
    fun toTCCustomEvent(): TCCustomEvent {
        val event = TCCustomEvent(name)
        for (customEntry in labels) {
            event.addAdditionalParameterIfNotBlank(customEntry.key, customEntry.value)
        }
        return event
    }
}
