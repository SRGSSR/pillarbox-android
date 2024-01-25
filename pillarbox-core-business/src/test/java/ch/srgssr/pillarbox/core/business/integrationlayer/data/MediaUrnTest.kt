/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MediaItem.LiveConfiguration
import androidx.media3.common.MediaItem.RequestMetadata
import androidx.media3.common.MediaMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

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

    @Test
    fun `create media item`() {
        val urn = "urn:rts:video:123345"
        val mediaItem = MediaUrn.createMediaItem(urn)

        assertEquals(urn, mediaItem.mediaId)
        assertEquals(ClippingConfiguration.Builder().build(), mediaItem.clippingConfiguration)
        assertNull(mediaItem.localConfiguration)
        assertEquals(LiveConfiguration.Builder().build(), mediaItem.liveConfiguration)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
        assertEquals(RequestMetadata.EMPTY, mediaItem.requestMetadata)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create media item, invalid urn`() {
        MediaUrn.createMediaItem("urn:rts:channel:tv:1234")
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
