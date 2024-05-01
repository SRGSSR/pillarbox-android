/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Credit
 */
sealed interface Credit : TimeRange, Parcelable {
    /**
     * Opening credits
     */
    @Parcelize
    data class Opening(
        override val start: Long,
        override val end: Long
    ) : Credit

    /**
     * Closing credits
     */
    @Parcelize
    data class Closing(
        override val start: Long,
        override val end: Long
    ) : Credit
}
