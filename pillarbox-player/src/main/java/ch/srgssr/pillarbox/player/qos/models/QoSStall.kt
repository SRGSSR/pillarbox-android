/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Information about stalls.
 *
 * @property count The number of stalls that have occurred, not as a result of a seek.
 * @property duration The total duration of the stalls, in milliseconds.
 */
data class QoSStall(
    val count: Int,
    val duration: Long,
)
