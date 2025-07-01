/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import androidx.annotation.StringRes
import ch.srgssr.pillarbox.core.business.R
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import java.io.IOException
import kotlin.time.Instant

/**
 * An exception thrown when a [Chapter] is blocked for a specific reason. The specific reason is indicated by the type of [BlockReasonException]
 * thrown.
 *
 * Each subclass of [BlockReasonException] corresponds to a specific [BlockReason] and provides a [messageResId] containing a user-friendly localized
 * message describing the block reason.
 *
 * **Checking the blocking reason**
 *
 * ```kotlin
 * val exception: BlockReasonException
 * when (exception) {
 *     is BlockReasonException.GeoBlock -> Log.d("Pillarbox", "This chapter is geo-blocked")
 *     is BlockReasonException.StartDate -> Log.d("Pillarbox", "This chapter will be available on ${exception.instant}.")
 *     is BlockReasonException.EndDate -> Log.d("Pillarbox", "This chapter is no longer available since ${exception.instant}.")
 *     // Handle other types...
 * }
 * ```
 *
 * @param message The exception message.
 */
sealed class BlockReasonException(message: String) : IOException(message) {
    /**
     * An Android resource id pointing to a localized string describing the block reason.
     */
    @StringRes
    open val messageResId: Int = R.string.blockReason_unknown

    private constructor(blockReason: BlockReason) : this(blockReason.name)

    /**
     * Represents an exception thrown when a [Chapter] is blocked due to its start date being in the future. This corresponds to
     * [BlockReason.STARTDATE].
     *
     * @property instant The [Instant] when the content will become available. This can be `null` if the start date is not known.
     */
    class StartDate(val instant: Instant?) : BlockReasonException(BlockReason.STARTDATE) {
        override val messageResId: Int
            get() = R.string.blockReason_startDate
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked due to reaching its end date. This corresponds to [BlockReason.ENDDATE].
     *
     * @property instant The [Instant] when the content became unavailable. This can be `null` if the start date is not known.
     */
    class EndDate(val instant: Instant?) : BlockReasonException(BlockReason.ENDDATE) {
        override val messageResId: Int
            get() = R.string.blockReason_endDate
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked due to legal reasons. This corresponds to [BlockReason.LEGAL].
     */
    class Legal : BlockReasonException(BlockReason.LEGAL) {
        override val messageResId: Int
            get() = R.string.blockReason_legal
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked due to an age rating of 18. This corresponds to [BlockReason.AGERATING18].
     */
    class AgeRating18 : BlockReasonException(BlockReason.AGERATING18) {
        override val messageResId: Int
            get() = R.string.blockReason_ageRating18
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked due to an age rating of 12. This corresponds to [BlockReason.AGERATING12].
     */
    class AgeRating12 : BlockReasonException(BlockReason.AGERATING12) {
        override val messageResId: Int
            get() = R.string.blockReason_ageRating12
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked due to geographical restrictions. This corresponds to [BlockReason.GEOBLOCK].
     */
    class GeoBlock : BlockReasonException(BlockReason.GEOBLOCK) {
        override val messageResId: Int
            get() = R.string.blockReason_geoBlock
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked for commercial reason. This corresponds to [BlockReason.COMMERCIAL].
     */
    class Commercial : BlockReasonException(BlockReason.COMMERCIAL) {
        override val messageResId: Int
            get() = R.string.blockReason_commercial
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked for journalistic reason. This corresponds to [BlockReason.JOURNALISTIC].
     */
    class Journalistic : BlockReasonException(BlockReason.JOURNALISTIC) {
        override val messageResId: Int
            get() = R.string.blockReason_journalistic
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked due to the usage of a VPN or a proxy. This corresponds to
     * [BlockReason.VPNORPROXYDETECTED].
     */
    class VPNOrProxyDetected : BlockReasonException(BlockReason.VPNORPROXYDETECTED) {
        override val messageResId: Int
            get() = R.string.blockReason_vpn_or_proxy_detected
    }

    /**
     * Represents an exception thrown when a [Chapter] is blocked for an unknown reason. This corresponds to [BlockReason.UNKNOWN].
     */
    class Unknown : BlockReasonException(BlockReason.UNKNOWN)
}
