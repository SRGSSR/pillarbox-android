/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.Serializable

/**
 * Represents a segment of a media item.
 *
 * @property urn The URN of the segment.
 * @property title The title of the segment.
 * @property markIn The start time of the segment, in milliseconds.
 * @property markOut The end time of the segment, in milliseconds.
 * @property blockReason The reason why the segment is blocked, if applicable.
 */
@Serializable
data class Segment(
    val urn: String,
    val title: String,
    val markIn: Long,
    val markOut: Long,
    val blockReason: BlockReason? = null
)
