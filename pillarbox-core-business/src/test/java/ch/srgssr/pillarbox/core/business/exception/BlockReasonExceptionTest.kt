/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import ch.srgssr.pillarbox.core.business.R
import ch.srgssr.pillarbox.core.business.extension.getBlockReasonExceptionOrNull
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaType
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BlockReasonExceptionTest {
    @Test
    fun `BlockReasonException created with a BlockReason`() {
        val expectedExceptionForBlockReason = mapOf(
            BlockReason.STARTDATE to BlockReasonException.StartDate::class.java,
            BlockReason.ENDDATE to BlockReasonException.EndDate::class.java,
            BlockReason.LEGAL to BlockReasonException.Legal::class.java,
            BlockReason.AGERATING18 to BlockReasonException.AgeRating18::class.java,
            BlockReason.AGERATING12 to BlockReasonException.AgeRating12::class.java,
            BlockReason.GEOBLOCK to BlockReasonException.GeoBlock::class.java,
            BlockReason.COMMERCIAL to BlockReasonException.Commercial::class.java,
            BlockReason.JOURNALISTIC to BlockReasonException.Journalistic::class.java,
            BlockReason.UNKNOWN to BlockReasonException.Unknown::class.java,
        )
        BlockReason.entries.forEach { blockReason ->
            val chapter = Chapter(
                urn = "id",
                title = "chapter",
                imageUrl = "",
                mediaType = MediaType.VIDEO,
                blockReason = blockReason,
            )
            val exception = chapter.getBlockReasonExceptionOrNull()
            val expectedClass = expectedExceptionForBlockReason[blockReason]
            assertNotNull(exception)
            assertEquals(exception::class.java, expectedClass)
        }
    }

    @Test
    fun `Chapter without block reason returns null`() {
        val chapter = Chapter(
            urn = "id",
            title = "chapter",
            imageUrl = "",
            mediaType = MediaType.VIDEO,
        )
        assertNull(chapter.getBlockReasonExceptionOrNull())
    }

    @Test
    fun `Chapter with start date`() {
        val chapter = Chapter(
            urn = "id",
            title = "chapter",
            imageUrl = "",
            blockReason = BlockReason.STARTDATE,
            validFrom = Clock.System.now(),
            mediaType = MediaType.VIDEO,
        )
        val exception = chapter.getBlockReasonExceptionOrNull()
        assertIs<BlockReasonException.StartDate>(exception)
        assertEquals(chapter.validFrom, exception.instant)
    }

    @Test
    fun `Chapter with end date`() {
        val chapter = Chapter(
            urn = "id",
            title = "chapter",
            imageUrl = "",
            blockReason = BlockReason.ENDDATE,
            validTo = Clock.System.now(),
            mediaType = MediaType.VIDEO,
        )
        val exception = chapter.getBlockReasonExceptionOrNull()
        assertIs<BlockReasonException.EndDate>(exception)
        assertEquals(chapter.validTo, exception.instant)
    }

    @Test
    fun `get string resId for BlockReasonException`() {
        assertEquals(R.string.blockReason_ageRating12, BlockReasonException.AgeRating12().messageRestId)
        assertEquals(R.string.blockReason_ageRating18, BlockReasonException.AgeRating18().messageRestId)
        assertEquals(R.string.blockReason_commercial, BlockReasonException.Commercial().messageRestId)
        assertEquals(R.string.blockReason_endDate, BlockReasonException.EndDate(null).messageRestId)
        assertEquals(R.string.blockReason_geoBlock, BlockReasonException.GeoBlock().messageRestId)
        assertEquals(R.string.blockReason_legal, BlockReasonException.Legal().messageRestId)
        assertEquals(R.string.blockReason_startDate, BlockReasonException.StartDate(null).messageRestId)
        assertEquals(R.string.blockReason_journalistic, BlockReasonException.Journalistic().messageRestId)
        assertEquals(R.string.blockReason_unknown, BlockReasonException.Unknown().messageRestId)
    }
}
