/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business

import ch.srg.pillarbox.core.business.integrationlayer.data.MediaUrn
import ch.srg.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import org.junit.Assert
import org.junit.Test

class MediaUrnTest {

    @Test
    fun testEmpty() {
        val urn = ""
        Assert.assertFalse(urn.isValidMediaUrn())
    }

    @Test
    fun testNull() {
        val urn: String? = null
        Assert.assertFalse(urn.isValidMediaUrn())
    }

    @Test
    fun testVideoUrn() {
        val urn = "urn:rts:video:123345"
        Assert.assertTrue(urn.isValidMediaUrn())
    }

    @Test
    fun testAudioUrn() {
        val urn = "urn:rts:audio:123345"
        Assert.assertTrue(MediaUrn.isValid(urn))
    }

    @Test
    fun testRandomText() {
        val urn = "hello guys!"
        Assert.assertFalse(urn.isValidMediaUrn())
    }

    @Test
    fun testHttps() {
        val urn = "https://www.rts.ch/media"
        Assert.assertFalse(urn.isValidMediaUrn())
    }

    @Test
    fun testHttpsWithUrn() {
        val urn = "https://www.rts.ch/media/urn:rts:video:123345"
        Assert.assertFalse(urn.isValidMediaUrn())
    }

    @Test
    fun testShowUrn() {
        val urn = "urn:rts:show:tv:1234"
        Assert.assertFalse(MediaUrn.isValid(urn))
    }

    @Test
    fun testChannelUrn() {
        val urn = "urn:rts:channel:tv:1234"
        Assert.assertFalse(MediaUrn.isValid(urn))
    }
}
