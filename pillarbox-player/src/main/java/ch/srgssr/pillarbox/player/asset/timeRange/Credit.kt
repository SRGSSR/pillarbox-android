/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents an opening or a closing credit.
 */
sealed interface Credit : TimeRange, Parcelable {
    /**
     * Represents the opening credits of a media.
     */
    @Parcelize
    data class Opening(
        override val start: Long,
        override val end: Long
    ) : Credit

    /**
     * Represents the closing credits of a media.
     */
    @Parcelize
    data class Closing(
        override val start: Long,
        override val end: Long
    ) : Credit
}
