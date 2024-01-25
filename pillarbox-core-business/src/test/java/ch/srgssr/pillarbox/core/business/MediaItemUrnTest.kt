/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.core.net.toUri
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaItemUrnTest {
    @Test
    fun `MediaItemUrn with all parameters`() {
        val urn = "urn:rts:audio:3262363"
        val title = "Media title"
        val subtitle = "Media subtitle"
        val artworkUri = "Artwork uri".toUri()
        val mediaItem = MediaItemUrn(
            urn = urn,
            title = title,
            subtitle = subtitle,
            artworkUri = artworkUri,
        )

        assertEquals(urn, mediaItem.mediaId)
        assertEquals(title, mediaItem.mediaMetadata.title)
        assertEquals(subtitle, mediaItem.mediaMetadata.subtitle)
        assertEquals(artworkUri, mediaItem.mediaMetadata.artworkUri)
    }

    @Test
    fun `MediaItemUrn with urn only`() {
        val urn = "urn:rts:audio:3262363"
        val mediaItem = MediaItemUrn(
            urn = urn,
        )

        assertEquals(urn, mediaItem.mediaId)
        assertNull(mediaItem.mediaMetadata.title)
        assertNull(mediaItem.mediaMetadata.subtitle)
        assertNull(mediaItem.mediaMetadata.artworkUri)
    }
}
