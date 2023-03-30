/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

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
 * @property customLabels The event custom labels.
 */
data class Event(
    val name: String,
    val type: String? = null,
    val value: String? = null,
    val source: String? = null,
    val extra1: String? = null,
    val extra2: String? = null,
    val extra3: String? = null,
    val extra4: String? = null,
    val extra5: String? = null,
    override val customLabels: CustomLabels? = null
) : BaseEvent
