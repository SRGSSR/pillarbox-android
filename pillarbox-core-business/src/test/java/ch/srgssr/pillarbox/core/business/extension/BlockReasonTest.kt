/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.extension

import ch.srgssr.pillarbox.core.business.R
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockReasonTest {
    @Test
    fun `get string resId for BlockReason`() {
        assertEquals(R.string.blockReason_ageRating12, BlockReason.AGERATING12.getStringResId())
        assertEquals(R.string.blockReason_ageRating18, BlockReason.AGERATING18.getStringResId())
        assertEquals(R.string.blockReason_commercial, BlockReason.COMMERCIAL.getStringResId())
        assertEquals(R.string.blockReason_endDate, BlockReason.ENDDATE.getStringResId())
        assertEquals(R.string.blockReason_geoBlock, BlockReason.GEOBLOCK.getStringResId())
        assertEquals(R.string.blockReason_legal, BlockReason.LEGAL.getStringResId())
        assertEquals(R.string.blockReason_startDate, BlockReason.STARTDATE.getStringResId())
        assertEquals(R.string.blockReason_unknown, BlockReason.UNKNOWN.getStringResId())
    }
}
