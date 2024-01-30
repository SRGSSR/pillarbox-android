/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.akamai

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AkamaiTokenProviderTest {

    @Test
    fun `getAcl() from a simple uri return null`() {
        val uri = Uri.parse("https://www.fake.url")
        val acl = AkamaiTokenProvider.getAcl(uri)
        assertNull(acl)
    }

    @Test
    fun `getAcl() from a long uri without extension at the end`() {
        val uri = Uri.parse("https://fake.url/content/not/playingLive/with/a/very/long/pat")
        val expectedAcl = "/content/not/playingLive/with/a/very/long/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        assertNotNull(acl)
        assertEquals(expectedAcl, acl)
    }

    @Test
    fun `getAcl() from an uri with very long path and finishing with an extension`() {
        val uri = Uri.parse("https://fake.url/content/not/playingLive/with/a/very/long/pat/playlist.mp3")
        val expectedAcl = "/content/not/playingLive/with/a/very/long/pat/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        assertNotNull(acl)
        assertEquals(expectedAcl, acl)
    }

    @Test
    fun `getAcl() from an uri with short path and finishing with an extension`() {
        val uri = Uri.parse("https://fake.url/content/playlist.m3u8")
        val expectedAcl = "/content/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        assertNotNull(acl)
        assertEquals(expectedAcl, acl)
    }

    @Test
    fun `getAcl() from a uri with no path but end with an extension`() {
        val uri = Uri.parse("https://fake.url/playlist.m3u8")
        val expectedAcl = "/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        assertNotNull(acl)
        assertEquals(expectedAcl, acl)
    }

    @Test
    fun `getAcl() from a live uri`() {
        val uri = Uri.parse("https://fake.url/content/hls/playingLive/additional/path")
        val expectedAcl = "/content/hls/playingLive/additional/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        assertNotNull(acl)
        assertEquals(expectedAcl, acl)
    }

    @Test
    fun `getAcl() from an uri with query parameters`() {
        val uri = Uri.parse("https://srgssrch.akamaized.net/hls/live/2022027/srgssr-hls-stream15-ch-dvr/master.m3u8?start=1697860830&end=1697867100")
        val expectedAcl = "/hls/live/2022027/srgssr-hls-stream15-ch-dvr/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        assertNotNull(acl)
        assertEquals(expectedAcl, acl)
    }

    @Test
    fun `AppendTokenToUri add token at the end of the uri`() {
        val uri = Uri.parse("https://srgssrch.akamaized.net/hls/live/2022027/srgssr-hls-stream15-ch-dvr/master.m3u8?start=1697860830&end=1697867100")
        val fakeToken = AkamaiTokenProvider.Token(authParams = "Token")
        val actual = AkamaiTokenProvider.appendTokenToUri(uri, fakeToken)
        assertNotEquals(uri, actual)
        assertTrue(actual.toString().contains(uri.toString()), "Contains base url")
    }
}
