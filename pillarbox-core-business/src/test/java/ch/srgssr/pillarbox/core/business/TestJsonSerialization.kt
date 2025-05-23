/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.player.network.jsonSerializer
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestJsonSerialization {
    @Test
    fun testChapterValidJson() {
        val json = "{\"urn\":\"urn:srf:video:12343\",\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\",\"blockReason\": \"UNKNOWN\"," +
            "\"mediaType\": \"VIDEO\",\"type\":\"EPISODE\"}"
        val chapter = jsonSerializer.decodeFromString<Chapter>(json)
        assertNotNull(chapter)
        assertEquals(BlockReason.UNKNOWN, chapter.blockReason)
    }

    @Test(expected = SerializationException::class)
    fun testChapterValidJsonUnknownBlockreason() {
        val json = "{\"urn\":\"urn:srf:video:12343\",\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\",\"blockReason\": \"TOTO\"," +
            "\"mediaType\": \"VIDEO\",\"type\":\"EPISODE\"}"
        val chapter = jsonSerializer.decodeFromString<Chapter>(json)
        assertNotNull(chapter)
        assertNotNull(chapter.blockReason)
    }

    @Test(expected = SerializationException::class)
    fun testChapterWithNullUrnJson() {
        val json = "{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        jsonSerializer.decodeFromString<Chapter>(json)
    }

    @Test(expected = SerializationException::class)
    fun testMediaCompositionWithInvalidJson() {
        val json = "{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        jsonSerializer.decodeFromString<MediaComposition>(json)
    }

    @Test(expected = SerializationException::class)
    fun testMediaCompositionWithNullJsonFields() {
        val json = "{\"chapterList\": [{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}]}"
        val mediaComposition = jsonSerializer.decodeFromString<MediaComposition>(json)
        assertNotNull(mediaComposition)
    }

    @Test
    fun testMediaCompositionValidJson() {
        val json = """
{
  "chapterUrn": "urn:srf:video:12343",
  "chapterList": [
    {
      "urn": "urn:srf:video:12343",
      "title": "Chapter title",
      "imageUrl": "https://image.png",
      "mediaType": "VIDEO",
      "type": "EPISODE"
    }
  ]
}
"""
        val mediaComposition = jsonSerializer.decodeFromString<MediaComposition>(json)
        assertNotNull(mediaComposition)
    }
}
