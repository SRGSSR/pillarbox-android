/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.Serializable

/**
 * Represents a time interval.
 *
 * @property markIn The start time of the interval, in milliseconds.
 * @property markOut The end time of the interval, in milliseconds.
 * @property type The type of the time interval.
 */
@Serializable
data class TimeInterval(
    val markIn: Long?,
    val markOut: Long?,
    val type: TimeIntervalType?,
)
