/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MediaUrnTest {
    @Test
    fun `is valid media urn`() {
        urnData.forEach { (urn, isValid) ->
            assertEquals(isValid, urn.isValidMediaUrn(), "Expected '$urn' to be ${if (isValid) "a valid" else "an invalid"} urn")
        }
    }

    @Test
    fun `is valid media urn, urn is null`() {
        val urn: String? = null

        assertFalse(urn.isValidMediaUrn())
    }

    @Test
    fun `MediaUrn is valid`() {
        urnData.forEach { (urn, isValid) ->
            assertEquals(isValid, MediaUrn.isValid(urn), "Expected '$urn' to be ${if (isValid) "a valid" else "an invalid"} urn")
        }
    }

    private companion object {
        private val urnData = mapOf(
            "" to false,
            " " to false,
            "Hello guys!" to false,
            "https://www.rts.ch/media" to false,
            "https://www.rts.ch/media/urn:rts:video:123345" to false,
            "urn:rts:channel:tv:1234" to false,
            "urn:rts:show:tv:1234" to false,
            "urn:rts:audio:123345" to true,
            "urn:rts:video:123345" to true,
        )
    }
}
