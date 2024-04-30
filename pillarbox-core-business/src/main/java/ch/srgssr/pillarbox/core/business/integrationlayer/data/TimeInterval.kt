/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.Serializable

/**
 * Time interval
 *
 * @property markIn
 * @property markOut
 * @property type
 */
@Serializable
data class TimeInterval(
    val markIn: Long?,
    val markOut: Long?,
    val type: TimeIntervalType?,
)
