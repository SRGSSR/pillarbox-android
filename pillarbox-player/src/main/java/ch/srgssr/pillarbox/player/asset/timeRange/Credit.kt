/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an opening or a closing credit.
 */
@Serializable
sealed interface Credit : TimeRange, Parcelable {
    /**
     * Represents the opening credits of a media.
     */
    @Parcelize
    @Serializable
    @SerialName("OpeningCredit")
    data class Opening(
        override val start: Long,
        override val end: Long
    ) : Credit

    /**
     * Represents the closing credits of a media.
     */
    @Parcelize
    @Serializable
    @SerialName("ClosingCredit")
    data class Closing(
        override val start: Long,
        override val end: Long
    ) : Credit
}
