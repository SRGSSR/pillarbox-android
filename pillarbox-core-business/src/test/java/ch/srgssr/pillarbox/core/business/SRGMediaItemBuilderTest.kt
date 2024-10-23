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
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class SRGMediaItemBuilderTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Check with invalid urn`() {
        val urn = "urn:rts:show:3262363"
        SRGMediaItem(urn)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Check with empty mediaItem`() {
        MediaItem.EMPTY.buildUpon { }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Check with invalid mediaId`() {
        SRGMediaItem("1234")
    }

    @Test
    fun `Check default arguments`() {
        val urn = "urn:rts:audio:3262363"
        val mediaItem = SRGMediaItem(urn)
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
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
        val mediaItem = SRGMediaItem(urn) {
            mediaMetadata(metadata)
        }
        val localConfiguration = mediaItem.localConfiguration
        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(metadata, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set host to Stage`() {
        val urn = "urn:rts:audio:3262363"
        val ilHost = IlHost.STAGE
        val mediaItem = SRGMediaItem(urn) {
            host(ilHost)
        }
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(ilHost), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set vector to TV`() {
        val urn = "urn:rts:audio:3262363"
        val vector = Vector.TV
        val mediaItem = SRGMediaItem(urn) {
            vector(vector)
        }
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(vector = vector), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check no vector`() {
        val urn = "urn:rts:audio:3262363"
        val vector = ""
        val mediaItem = SRGMediaItem(urn) {
            vector(vector)
        }
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(vector = vector), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check uri from existing MediaItem`() {
        val urn = "urn:rts:audio:3262363"
        val ilHost = IlHost.STAGE
        val inputMediaItem = MediaItem.Builder()
            .setUri("${ilHost}integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=${Vector.TV}")
            .build()
        val mediaItem = SRGMediaItemBuilder(inputMediaItem).build()
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(ilHost, vector = Vector.TV), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
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
        val mediaItem = SRGMediaItem(urn) {
            host(IlHost.PROD)
            vector(Vector.MOBILE)
            urn(urn2)
        }
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn2.toIlUri(vector = Vector.MOBILE), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn2, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set forceSAM`() {
        val urn = "urn:rts:audio:3262363"
        val ilHost = IlHost.STAGE
        val forceSAM = true
        val mediaItem = SRGMediaItem(urn) {
            host(ilHost)
            forceSAM(forceSAM)
        }
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(ilHost, forceSAM = forceSAM), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set forceSAM from existing URL`() {
        val urn = "urn:rts:audio:3262363"
        val ilHost = IlHost.STAGE
        val forceSAM = true
        val inputMediaItem = MediaItem.Builder()
            .setUri("${IlHost.PROD}sam/integrationlayer/2.1/mediaComposition/byUrn/$urn?forceSAM=true")
            .build()
        val mediaItem = SRGMediaItemBuilder(inputMediaItem).apply {
            host(ilHost)
            forceSAM(true)
        }.build()
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(ilHost, forceSAM = forceSAM), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    @Test
    fun `Check set forceLocation`() {
        val urn = "urn:rts:audio:3262363"
        val ilHost = IlHost.STAGE
        val forceLocation = "CH"
        val mediaItem = SRGMediaItem(urn) {
            host(ilHost)
            forceLocation(forceLocation)
        }
        val localConfiguration = mediaItem.localConfiguration

        assertNotNull(localConfiguration)
        assertEquals(urn.toIlUri(ilHost, forceLocation = forceLocation), localConfiguration.uri)
        assertEquals(MimeTypeSrg, localConfiguration.mimeType)
        assertEquals(urn, mediaItem.mediaId)
        assertEquals(MediaMetadata.EMPTY, mediaItem.mediaMetadata)
    }

    companion object {
        fun String.toIlUri(
            host: URL = IlHost.DEFAULT,
            vector: String = Vector.MOBILE,
            forceSAM: Boolean = false,
            forceLocation: String? = null,
        ): Uri {
            val samPath = if (forceSAM) "sam/" else ""
            val queryParameters = listOfNotNull(
                if (forceSAM) "forceSAM" to true else null,
                if (forceLocation != null) "forceLocation" to forceLocation else null,
                if (vector.isNotBlank()) "vector" to vector else null,
                "onlyChapters" to true,
            ).joinToString(separator = "&") { (name, value) ->
                "$name=$value"
            }

            return "${host}${samPath}integrationlayer/2.1/mediaComposition/byUrn/$this?$queryParameters".toUri()
        }
    }
}
