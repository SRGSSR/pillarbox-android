/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageView
import com.tagcommander.lib.serverside.events.TCCustomEvent
import com.tagcommander.lib.serverside.events.TCEvent
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
     * Custom label Key for push notification source
     */
    private const val KEY_FROM_PUSH_NOTIFICATION = "accessed_after_push_notification"

    /**
     * Convert into TagCommander event.
     *
     * @return [TCCustomEvent]
     */
    fun Event.toTCCustomEvent(): TCCustomEvent {
        val event = TCCustomEvent(TC_EVENT_NAME)
        event.addAdditionalParameter(EVENT_NAME, name)
        event.addAdditionalParameterIfNotBlank(EVENT_TYPE, type)
        event.addAdditionalParameterIfNotBlank(EVENT_VALUE, value)
        event.addAdditionalParameterIfNotBlank(EVENT_SOURCE, source)
        event.addAdditionalParameterIfNotBlank(EVENT_EXTRA_1, extra1)
        event.addAdditionalParameterIfNotBlank(EVENT_EXTRA_2, extra2)
        event.addAdditionalParameterIfNotBlank(EVENT_EXTRA_3, extra3)
        event.addAdditionalParameterIfNotBlank(EVENT_EXTRA_4, extra4)
        event.addAdditionalParameterIfNotBlank(EVENT_EXTRA_5, extra5)
        return event
    }

    private fun TCEvent.addAdditionalParameterIfNotBlank(key: String, data: String?) {
        if (!data.isNullOrBlank()) {
            addAdditionalParameter(key, data)
        }
    }

    /**
     * Convert into a TagCommander event
     *
     * @param distributor The [AnalyticsConfig.BuDistributor] to send with this event.
     * @return [TCPageViewEvent]
     */
    fun PageView.toTCCustomEvent(distributor: String): TCPageViewEvent {
        val pageViewEvent = TCPageViewEvent()
        pageViewEvent.pageType = title
        for (i in levels.indices) {
            pageViewEvent.addAdditionalParameter(NAVIGATION_LEVEL_I + (i + 1), levels[i])
        }
        pageViewEvent.addAdditionalParameter(NAVIGATION_BU_DISTRIBUTER, distributor)
        pageViewEvent.addAdditionalParameter(KEY_FROM_PUSH_NOTIFICATION, fromPushNotification.toString())
        return pageViewEvent
    }
}
