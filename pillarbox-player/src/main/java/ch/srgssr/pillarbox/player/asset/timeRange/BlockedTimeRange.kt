/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a time range that is blocked for playback. When the player reaches the [start] time of a blocked range, it will immediately seek to the
 * [end] time, effectively skipping the blocked portion. This behavior is enforced regardless of the [reason] or [id] associated with the block.
 *
 * @property start The start position, in milliseconds.
 * @property end The end position, in milliseconds.
 * @property reason An optional string describing the reason for the block.
 * @property id An optional unique identifier for the block.
 */
@Parcelize
data class BlockedTimeRange(
    override val start: Long,
    override val end: Long,
    val reason: String? = null,
    val id: String? = null,
) : TimeRange, Parcelable
