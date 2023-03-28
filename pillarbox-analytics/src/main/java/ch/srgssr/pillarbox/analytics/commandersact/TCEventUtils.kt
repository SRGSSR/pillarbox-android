/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageEvent
import com.tagcommander.lib.serverside.events.TCCustomEvent
import com.tagcommander.lib.serverside.events.TCPageViewEvent

/**
 * CommandersAct event conversion
 */
object TCEventUtils {
    // Event keys
    private const val TC_EVENT_NAME = "hidden_event"
    private const val EVENT_VALUE = "event_value"
    private const val EVENT_NAME = "event_name"
    private const val EVENT_TYPE = "event_type"
    private const val EVENT_SOURCE = "event_source"
    private const val EVENT_EXTRA_1 = "event_value_1"
    private const val EVENT_EXTRA_2 = "event_value_2"
    private const val EVENT_EXTRA_3 = "event_value_3"
    private const val EVENT_EXTRA_4 = "event_value_4"
    private const val EVENT_EXTRA_5 = "event_value_5"

    // Page View
    private const val NAVIGATION_LEVEL_I = "navigation_level_"
    private const val NAVIGATION_BU_DISTRIBUTER = "navigation_bu_distributer"

    /**
     * Convert into TagCommander event.
     *
     * @return [TCCustomEvent]
     */
    fun Event.toTCCustomEvent(): TCCustomEvent {
        val event = TCCustomEvent(TC_EVENT_NAME)
        event.addAdditionalParameter(EVENT_NAME, name)
        type?.let {
            event.addAdditionalParameter(EVENT_TYPE, it)
        }
        value?.let {
            event.addAdditionalParameter(EVENT_VALUE, it)
        }
        source?.let {
            event.addAdditionalParameter(EVENT_SOURCE, it)
        }
        extra1?.let { event.addAdditionalParameter(EVENT_EXTRA_1, it) }
        extra2?.let { event.addAdditionalParameter(EVENT_EXTRA_2, it) }
        extra3?.let { event.addAdditionalParameter(EVENT_EXTRA_3, it) }
        extra4?.let { event.addAdditionalParameter(EVENT_EXTRA_4, it) }
        extra5?.let { event.addAdditionalParameter(EVENT_EXTRA_5, it) }
        customLabels?.let {
            for (entry in it.entries) {
                event.addAdditionalParameter(entry.key, entry.value)
            }
        }
        return event
    }

    /**
     * Convert into a TagCommander event
     *
     * @param distributor The [AnalyticsConfig.BuDistributor] to send with this event.
     * @return [TCPageViewEvent]
     */
    fun PageEvent.toTCCustomEvent(distributor: String): TCPageViewEvent {
        val pageViewEvent = TCPageViewEvent()
        pageViewEvent.pageType = title
        for (i in levels.indices) {
            pageViewEvent.addAdditionalParameter(NAVIGATION_LEVEL_I + (i + 1), levels[i])
        }
        pageViewEvent.addAdditionalParameter(NAVIGATION_BU_DISTRIBUTER, distributor)
        customLabels?.let {
            for (entry in it.entries) {
                pageViewEvent.addAdditionalParameter(entry.key, entry.value)
            }
        }
        return pageViewEvent
    }
}
