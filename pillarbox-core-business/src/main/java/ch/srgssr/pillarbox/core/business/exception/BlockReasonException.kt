/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import androidx.annotation.StringRes
import ch.srgssr.pillarbox.core.business.R
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import kotlinx.datetime.Instant
import java.io.IOException

/**
 * Block reason exception
 */
sealed class BlockReasonException(message: String) : IOException(message) {
    /**
     * The Android resource id of the message to display.
     */
    @StringRes
    open val messageRestId: Int = R.string.blockReason_unknown

    private constructor(blockReason: BlockReason) : this(blockReason.name)

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.STARTDATE].
     *
     * @property instant The [Instant] when the content will be available.
     */
    class StartDate(val instant: Instant?) : BlockReasonException(BlockReason.STARTDATE) {
        override val messageRestId: Int
            get() = R.string.blockReason_startDate
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.ENDDATE].
     *
     * @property instant The [Instant] since it is unavailable.
     */
    class EndDate(val instant: Instant?) : BlockReasonException(BlockReason.ENDDATE) {
        override val messageRestId: Int
            get() = R.string.blockReason_endDate
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.LEGAL].
     */
    class Legal : BlockReasonException(BlockReason.LEGAL) {
        override val messageRestId: Int
            get() = R.string.blockReason_legal
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.AGERATING18].
     */
    class AgeRating18 : BlockReasonException(BlockReason.AGERATING18) {
        override val messageRestId: Int
            get() = R.string.blockReason_ageRating18
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.AGERATING12].
     */
    class AgeRating12 : BlockReasonException(BlockReason.AGERATING12) {
        override val messageRestId: Int
            get() = R.string.blockReason_ageRating12
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.GEOBLOCK].
     */
    class GeoBlock : BlockReasonException(BlockReason.GEOBLOCK) {
        override val messageRestId: Int
            get() = R.string.blockReason_geoBlock
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.COMMERCIAL].
     */
    class Commercial : BlockReasonException(BlockReason.COMMERCIAL) {
        override val messageRestId: Int
            get() = R.string.blockReason_commercial
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.JOURNALISTIC].
     */
    class Journalistic : BlockReasonException(BlockReason.JOURNALISTIC) {
        override val messageRestId: Int
            get() = R.string.blockReason_journalistic
    }

    /**
     * [BlockReasonException] when [Chapter.blockReason] is [BlockReason.UNKNOWN].
     */
    class Unknown : BlockReasonException(BlockReason.UNKNOWN)
}
