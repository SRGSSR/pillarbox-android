/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import com.tagcommander.lib.serverside.events.TCPageViewEvent

/**
 * Represents a page view event for Commanders Act.
 *
 * This class encapsulates the data required to track a page view event, including the page name, type, navigation levels, and custom labels.
 *
 * @property name The name of the page being viewed. This property cannot be blank.
 * @property type The type of the page. This property cannot be blank.
 * @property levels A list of strings representing the navigation levels of the page. Defaults to an empty list.
 * @property labels A map of custom labels to be associated with the page view event. Blank values are ignored and not sent. Defaults to an empty map.
 *
 * @throws IllegalArgumentException If [name] or [type] is blank.
 */
class CommandersActPageView(
    val name: String,
    val type: String,
    val levels: List<String> = emptyList(),
    val labels: Map<String, String> = emptyMap()
) {
    init {
        require(name.isNotBlank()) { "Name can't be blank!" }
        require(type.isNotBlank()) { "Type can't be blank!" }
    }

    /**
     * Converts this event to a `TCPageViewEvent`.
     *
     * @param vendor The [vendor][AnalyticsConfig.Vendor] associated with this event.
     *
     * @return A `TCPageViewEvent` instance populated with data from this instance and the provided vendor.
     */
    fun toTCPageViewEvent(vendor: AnalyticsConfig.Vendor): TCPageViewEvent {
        val tcEvent = TCPageViewEvent()
        for (customEntry in labels) {
            tcEvent.addAdditionalParameterIfNotBlank(customEntry.key, customEntry.value)
        }
        tcEvent.pageName = name
        tcEvent.pageType = type
        for (i in levels.indices) {
            tcEvent.addAdditionalProperty(CommandersActLabels.NAVIGATION_LEVEL_I.label + (i + 1), levels[i])
        }
        tcEvent.addAdditionalProperty(CommandersActLabels.CONTENT_BU_OWNER.label, vendor.toString())
        return tcEvent
    }
}
