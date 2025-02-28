/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

/**
 * This enum defines the labels used for events sent to Commanders Act.
 *
 * @property label The label used by Commanders Act.
 */
@Suppress("UndocumentedPublicProperty")
enum class CommandersActLabels(val label: String) {
    // Event keys
    EVENT_VALUE("event_value"),
    EVENT_TYPE("event_type"),
    EVENT_SOURCE("event_source"),
    EVENT_EXTRA_1("event_value_1"),
    EVENT_EXTRA_2("event_value_2"),
    EVENT_EXTRA_3("event_value_3"),
    EVENT_EXTRA_4("event_value_4"),
    EVENT_EXTRA_5("event_value_5"),

    // Page View
    NAVIGATION_LEVEL_I("navigation_level_"),
    NAVIGATION_CONTENT_BU_OWNER("content_bu_owner"),

    // User consent
    CONSENT_SERVICES("consent_services")
}
