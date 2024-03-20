/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.Serializable

/**
 * Segment
 *
 * @property blockReason
 */
@Serializable
data class Segment(
    val urn: String,
    val title: String,
    val markIn: Long,
    val markOut: Long,
    val blockReason: BlockReason? = null
)
