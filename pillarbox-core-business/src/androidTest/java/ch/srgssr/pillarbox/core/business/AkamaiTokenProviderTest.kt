/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
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
        val uri = Uri.parse("https://host")
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
    fun testAclShotPath() {
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
        val expectedAcl = "/hls/playingLive/*"
        val acl = AkamaiTokenProvider.getAcl(uri)
        Assert.assertNotNull(acl)
        Assert.assertEquals(expectedAcl, acl)
    }
}
