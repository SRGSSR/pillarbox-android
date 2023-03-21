/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import org.junit.Assert
import org.junit.Test

class TestJsonSerialization {

    @Test
    fun testValidJson() {
        val json = "{\"urn\":\"urn:srf:video:12343\",\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        // Like build with retrofit!
        val moshi = Moshi.Builder().build()
        val chapter = moshi.adapter(Chapter::class.java).fromJson(json)
        Assert.assertNotNull(chapter)
    }

    @Test(expected = JsonDataException::class)
    fun testChapterWithNullUrn() {
        val json = "{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        // Like build with retrofit!
        val moshi = Moshi.Builder().build()
        val chapter = moshi.adapter(Chapter::class.java).fromJson(json)
        Assert.assertNotNull(chapter)
    }

    @Test(expected = JsonDataException::class)
    fun testMediaCompositionWithInvalidJson() {
        val json = "{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}"
        // Like build with retrofit!
        val moshi = Moshi.Builder().build()
        val chapter = moshi.adapter(Chapter::class.java).fromJson(json)
        Assert.assertNotNull(chapter)
    }

    @Test(expected = JsonDataException::class)
    fun testMediaCompositionWithNullJsonFields() {
        val json = "{\"chapterList\": [{\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}]}"
        // Like build with retrofit!
        val moshi = Moshi.Builder().build()
        val mediaComposition = moshi.adapter(MediaComposition::class.java).fromJson(json)
        Assert.assertNotNull(mediaComposition)
    }

    @Test
    fun testMediaCompositionValidJson() {
        val json =
            "{\"chapterUrn\":\"urn:srf:video:12343\" ,\"chapterList\": [{\"urn\":\"urn:srf:video:12343\",\"title\":\"Chapter title\",\"imageUrl\":\"https://image.png\"}]}"
        // Like build with retrofit!
        val moshi = Moshi.Builder().build()
        val mediaComposition = moshi.adapter(MediaComposition::class.java).fromJson(json)
        Assert.assertNotNull(mediaComposition)
    }
}
