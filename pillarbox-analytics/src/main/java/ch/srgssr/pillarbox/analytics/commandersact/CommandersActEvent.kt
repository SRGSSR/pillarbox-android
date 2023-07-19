/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
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
 * @property type The event type.
 * @property value The event value.
 * @property source The event source.
 * @property extra1 The event extra1.
 * @property extra2 The event extra2.
 * @property extra3 The event extra3.
 * @property extra4 The event extra4.
 * @property extra5 The event extra5.
 */
data class CommandersActEvent(
    val name: String,
    val type: String? = null,
    val value: String? = null,
    val source: String? = null,
    val extra1: String? = null,
    val extra2: String? = null,
    val extra3: String? = null,
    val extra4: String? = null,
    val extra5: String? = null
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
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_TYPE.label, type)
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_VALUE.label, value)
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_SOURCE.label, source)
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_EXTRA_1.label, extra1)
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_EXTRA_2.label, extra2)
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_EXTRA_3.label, extra3)
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_EXTRA_4.label, extra4)
        event.addAdditionalParameterIfNotBlank(CommandersActLabels.EVENT_EXTRA_5.label, extra5)
        return event
    }
}
