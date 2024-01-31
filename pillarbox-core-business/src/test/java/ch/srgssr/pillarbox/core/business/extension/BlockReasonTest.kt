/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.extension

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.R
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class BlockReasonTest {
    @Test
    fun `getString() for BlockReason via Context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals("To protect children this content is only available between 8PM and 6AM.", context.getString(BlockReason.AGERATING12))
        assertEquals("To protect children this content is only available between 10PM and 5AM.", context.getString(BlockReason.AGERATING18))
        assertEquals("This commercial content is not available.", context.getString(BlockReason.COMMERCIAL))
        assertEquals("This content is not available anymore.", context.getString(BlockReason.ENDDATE))
        assertEquals("This content is not available outside Switzerland.", context.getString(BlockReason.GEOBLOCK))
        assertEquals("This content is not available due to legal restrictions.", context.getString(BlockReason.LEGAL))
        assertEquals("This content is not available yet.", context.getString(BlockReason.STARTDATE))
        assertEquals("This content is not available.", context.getString(BlockReason.UNKNOWN))
    }

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
