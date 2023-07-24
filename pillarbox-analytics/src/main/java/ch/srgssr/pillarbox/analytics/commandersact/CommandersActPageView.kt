/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.commandersact.TCEventExtensions.addAdditionalParameterIfNotBlank
import com.tagcommander.lib.serverside.events.TCPageViewEvent

/**
 * Page event
 *
 * @property name The page event name.
 * @property type The page event type ie (Articles).
 * @property levels The page event levels.
 * @property labels The custom labels to send with this page view event. Blank values are not send.
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
     * Convert to [TCPageViewEvent]
     *
     * @param vendor The vendor of this event.
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
        tcEvent.addAdditionalProperty(CommandersActLabels.NAVIGATION_BU_DISTRIBUTER.label, vendor.toString())
        return tcEvent
    }
}
