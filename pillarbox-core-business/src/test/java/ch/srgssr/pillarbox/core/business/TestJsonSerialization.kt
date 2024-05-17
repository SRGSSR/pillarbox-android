/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srg.dataProvider.integrationlayer.data.remote.BlockReason
import ch.srg.dataProvider.integrationlayer.data.remote.Chapter
import ch.srg.dataProvider.integrationlayer.data.remote.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultHttpClient.jsonSerializer
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestJsonSerialization {
    @Test
    fun testChapterValidJson() {
        val json = """
{
  "id": "id",
  "urn": "urn:srf:video:12343",
  "title": "Chapter title",
  "imageUrl": "https://image.png",
  "blockReason": "UNKNOWN",
  "mediaType": "VIDEO",
  "vendor": "RTS",
  "type": "CLIP",
  "date": "2024-05-22T11:21:00+02:00",
  "duration": 120000
}
"""
        val chapter = jsonSerializer.decodeFromString<Chapter>(json)
        assertNotNull(chapter)
        assertEquals(BlockReason.UNKNOWN, chapter.blockReason)
    }

    @Test
    fun testChapterValidJsonUnknownBlockReason() {
        val json = """
{
  "id": "id",
  "urn": "urn:srf:video:12343",
  "title": "Chapter title",
  "imageUrl": "https://image.png",
  "blockReason": "TOTO",
  "mediaType": "VIDEO",
  "vendor": "RTS",
  "type": "CLIP",
  "date": "2024-05-22T11:21:00+02:00",
  "duration": 120000
}
"""
        val chapter = jsonSerializer.decodeFromString<Chapter>(json)
        assertNotNull(chapter)
        assertEquals(BlockReason.UNKNOWN, chapter.blockReason)
    }

    @Test(expected = SerializationException::class)
    fun testChapterWithNullUrnJson() {
        val json = """
{
  "id": "id",
  "title": "Chapter title",
  "imageUrl": "https://image.png",
  "blockReason": "TOTO",
  "mediaType": "VIDEO",
  "vendor": "RTS",
  "type": "CLIP",
  "date": "2024-05-22T11:21:00+02:00",
  "duration": 120000
}
"""
        jsonSerializer.decodeFromString<Chapter>(json)
    }

    @Test(expected = SerializationException::class)
    fun testMediaCompositionWithInvalidJson() {
        val json = """
{
  "title": "Chapter title",
  "imageUrl": "https://image.png"
}
"""
        jsonSerializer.decodeFromString<MediaComposition>(json)
    }

    @Test(expected = SerializationException::class)
    fun testMediaCompositionWithNullJsonFields() {
        val json = """
{
  "chapterList": [
    {
      "title": "Chapter title",
      "imageUrl": "https://image.png"
    }
  ]
}
"""
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
      "id": "id",
      "urn": "urn:srf:video:12343",
      "title": "Chapter title",
      "imageUrl": "https://image.png",
      "mediaType": "VIDEO",
      "vendor": "RTS",
      "type": "CLIP",
      "date": "2024-05-22T11:21:00+02:00",
      "duration": 120000
    }
  ]
}
"""
        val mediaComposition = jsonSerializer.decodeFromString<MediaComposition>(json)
        assertNotNull(mediaComposition)
    }
}
