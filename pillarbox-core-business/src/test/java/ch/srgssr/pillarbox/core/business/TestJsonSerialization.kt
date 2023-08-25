/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

class TestJsonSerialization {

    val jsonSerializer = Json { ignoreUnknownKeys = true }

    @Test
    fun testChapterValidJson() {
        val json = "{\"urn\":\"urn:srf:video:12343\",\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        val chapter = jsonSerializer.decodeFromString<Chapter>(json)
        Assert.assertNotNull(chapter)
    }

    @Test(expected = SerializationException::class)
    fun testChapterWithNullUrnJson() {
        val json = "{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        val chapter = jsonSerializer.decodeFromString<Chapter>(json)
        Assert.assertNotNull(chapter)
    }

    @Test(expected = SerializationException::class)
    fun testMediaCompositionWithInvalidJson() {
        val json = "{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        val mediaComposition = jsonSerializer.decodeFromString<MediaComposition>(json)
        Assert.assertNotNull(mediaComposition)
    }

    @Test(expected = SerializationException::class)
    fun testMediaCompositionWithNullJsonFields() {
        val json = "{\"chapterList\": [{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}]}"
        val mediaComposition = jsonSerializer.decodeFromString<MediaComposition>(json)
        Assert.assertNotNull(mediaComposition)
    }

    @Test
    fun testMediaCompositionValidJson() {
        val json =
            "{\"chapterUrn\":\"urn:srf:video:12343\" ,\"chapterList\": [{\"urn\":\"urn:srf:video:12343\",\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}]}"
        val mediaComposition = jsonSerializer.decodeFromString<MediaComposition>(json)
        Assert.assertNotNull(mediaComposition)
    }
}
