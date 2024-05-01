/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Blocked time range. The player will always seek to [end] when reaching [start], regardless of the [reason] or [id].
 *
 * @property start The start position, in milliseconds.
 * @property end The end position, in milliseconds.
 * @property reason The optional block reason.
 * @property id The optional id.
 */
@Parcelize
data class BlockedTimeRange(
    override val start: Long,
    override val end: Long,
    val reason: String? = null,
    val id: String? = null,
) : TimeRange, Parcelable
