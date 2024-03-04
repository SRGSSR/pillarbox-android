/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import ch.srgssr.pillarbox.core.business.source.MimeTypeSrg
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class MediaItemUrnTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Check with invalid urn`() {
        val urn = "urn:rts:show:3262363"
        SRGMediaItemBuilder(urn).build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Check with invalid mediaId`() {
        SRGMediaItemBuilder(MediaItem.Builder().setMediaId("1234").build()).build()
    }

    @Test
    fun `Check default arguments`() {
        val urn = "urn:rts:audio:3262363"
        val mediaItem = SRGMediaItemBuilder(urn).build()
        assertNotNull(mediaItem.localConfiguration)
        assertEquals(Uri.parse(expectedUrl(urn)), mediaItem.localConfiguration?.uri)
        assertEquals(MimeTypeSrg, mediaItem.localConfiguration?.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set MediaMetadata`() {
        val urn = "urn:rts:audio:3262363"
        val metadata = MediaMetadata.Builder()
            .setTitle("Media title")
            .setSubtitle("Media subtitle")
            .setArtworkUri("Artwork uri".toUri())
            .build()
        val mediaItem = SRGMediaItemBuilder(urn).apply {
            setMediaMetadata(metadata)
        }.build()
        assertNotNull(mediaItem.localConfiguration)
        assertEquals(Uri.parse(expectedUrl(urn)), mediaItem.localConfiguration?.uri)
        assertEquals(MimeTypeSrg, mediaItem.localConfiguration?.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(metadata, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set host to Stage`() {
        val urn = "urn:rts:audio:3262363"
        val mediaItem = SRGMediaItemBuilder(urn)
            .setHost(IlHost.STAGE)
            .build()
        assertNotNull(mediaItem.localConfiguration)
        assertEquals(Uri.parse(expectedUrl(urn, "il-stage.srgssr.ch")), mediaItem.localConfiguration?.uri)
        assertEquals(MimeTypeSrg, mediaItem.localConfiguration?.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set vector to TV`() {
        val urn = "urn:rts:audio:3262363"
        val mediaItem = SRGMediaItemBuilder(urn)
            .setVector(Vector.TV)
            .build()
        assertNotNull(mediaItem.localConfiguration)
        assertEquals(Uri.parse(expectedUrl(urn, "il.srgssr.ch", vector = Vector.TV)), mediaItem.localConfiguration?.uri)
        assertEquals(MimeTypeSrg, mediaItem.localConfiguration?.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check uri from existing MediaItem`() {
        val urn = "urn:rts:audio:3262363"
        val inputMediaItem = MediaItem.Builder()
            .setUri("https://il-stage.srgssr.ch/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=${Vector.TV}")
            .build()
        val mediaItem = SRGMediaItemBuilder(inputMediaItem).build()
        assertNotNull(mediaItem.localConfiguration)
        assertEquals(Uri.parse(expectedUrl(urn, "il-stage.srgssr.ch", vector = Vector.TV)), mediaItem.localConfiguration?.uri)
        assertEquals(MimeTypeSrg, mediaItem.localConfiguration?.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check uri from existing MediaItem changing parameters`() {
        val urn = "urn:rts:audio:3262363"
        val inputMediaItem = MediaItem.Builder()
            .setUri("https://il-stage.srgssr.ch/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=${Vector.TV}")
            .build()
        val urn2 = "urn:rts:audio:123456"
        val mediaItem = SRGMediaItemBuilder(inputMediaItem)
            .setHost(IlHost.PROD)
            .setVector(Vector.MOBILE)
            .setUrn(urn2)
            .build()
        assertNotNull(mediaItem.localConfiguration)
        assertEquals(Uri.parse(expectedUrl(urn2, "il.srgssr.ch", vector = Vector.MOBILE)), mediaItem.localConfiguration?.uri)
        assertEquals(MimeTypeSrg, mediaItem.localConfiguration?.mimeType)
        assertEquals(urn2, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    companion object {
        fun expectedUrl(urn: String, host: String = "il.srgssr.ch", vector: String = Vector.MOBILE): String {
            return "https://$host/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=$vector&onlyChapters=true"
        }
    }
}
