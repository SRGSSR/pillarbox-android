/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenProvider
import org.junit.Assert
import org.junit.Test

class AkamaiTokenProviderTest {

    @Test
    fun testInvalidUriForAcl() {
        val uri = Uri.parse("https://www.fake.url")
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNull(acl)
    }

    @Test
    fun testAclVeryLongPath() {
        val uri = Uri.parse("https://fake.url/content/not/playingLive/with/a/very/long/pat")
        val expectedAcl = "/content/not/playingLive/with/a/very/long/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNotNull(acl)
        Assert.assertEquals(expectedAcl, acl)
    }

    @Test
    fun testAclVeryLongPathWithExtension() {
        val uri = Uri.parse("https://fake.url/content/not/playingLive/with/a/very/long/pat/playlist.mp3")
        val expectedAcl = "/content/not/playingLive/with/a/very/long/pat/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNotNull(acl)
        Assert.assertEquals(expectedAcl, acl)
    }

    @Test
    fun testAclShortPath() {
        val uri = Uri.parse("https://fake.url/content/playlist.m3u8")
        val expectedAcl = "/content/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNotNull(acl)
        Assert.assertEquals(expectedAcl, acl)
    }

    @Test
    fun testAclNoPath() {
        val uri = Uri.parse("https://fake.url/playlist.m3u8")
        val expectedAcl = "/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNotNull(acl)
        Assert.assertEquals(expectedAcl, acl)
    }

    @Test
    fun testAclLive() {
        val uri = Uri.parse("https://fake.url/content/hls/playingLive/additional/path")
        val expectedAcl = "/content/hls/playingLive/additional/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNotNull(acl)
        Assert.assertEquals(expectedAcl, acl)
    }

    @Test
    fun testAclWithQueryParameters() {
        val uri = Uri.parse("https://srgssrch.akamaized.net/hls/live/2022027/srgssr-hls-stream15-ch-dvr/master.m3u8?start=1697860830&end=1697867100")
        val expectedAcl = "/hls/live/2022027/srgssr-hls-stream15-ch-dvr/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNotNull(acl)
        Assert.assertEquals(expectedAcl, acl)
    }

    @Test
    fun testAppendTokenToUri() {
        val uri = Uri.parse("https://srgssrch.akamaized.net/hls/live/2022027/srgssr-hls-stream15-ch-dvr/master.m3u8?start=1697860830&end=1697867100")
        val fakeToken = AkamaiTokenProvider.Token(authParams = "Token")
        val actual = AkamaiTokenProvider.appendTokenToUri(uri, fakeToken)
        Assert.assertNotEquals(uri, actual)
        Assert.assertTrue("Contains base url", actual.toString().contains(uri.toString()))
    }
}
