/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockReasonExceptionTest {
    @Test
    fun `BlockReasonException created with a BlockReason`() {
        BlockReason.entries.forEach { blockReason ->
            val exception = BlockReasonException(blockReason)

            assertEquals(blockReason.name, exception.message)
        }
    }

    @Test
    fun `BlockReasonException created with a message matching a BlockReason`() {
        BlockReason.entries.forEach { blockReason ->
            val exception = BlockReasonException(blockReason.name)

            assertEquals(blockReason.name, exception.message)
        }
    }

    @Test
    fun `BlockReasonException created with a message not matching a BlockReason`() {
        val exception = BlockReasonException("FOO_BAR")

        assertEquals(BlockReason.UNKNOWN.name, exception.message)
    }
}
