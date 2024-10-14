/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PillarboxPreloadManagerTest {
    private lateinit var preloadManager: PillarboxPreloadManager

    @BeforeTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preloadManager = PillarboxPreloadManager(context = context)
    }

    @AfterTest
    fun tearDown() {
        preloadManager.release()
    }

    @Test
    fun `initial state`() {
        assertEquals(C.INDEX_UNSET, preloadManager.currentPlayingIndex)
        assertEquals(0, preloadManager.sourceCount)
        assertNull(preloadManager.getMediaSource(VOD1))
    }

    @Test
    fun `add-remove media`() {
        preloadManager.currentPlayingIndex = 2
        preloadManager.add(VOD1, 1)
        preloadManager.add(VOD2, 2)
        preloadManager.add(VOD3, 3)
        preloadManager.add(VOD4, 4)
        preloadManager.invalidate()

        assertEquals(2, preloadManager.currentPlayingIndex)
        assertEquals(4, preloadManager.sourceCount)
        assertNotNull(preloadManager.getMediaSource(VOD1))
        assertNotNull(preloadManager.getMediaSource(VOD2))
        assertNotNull(preloadManager.getMediaSource(VOD3))
        assertNotNull(preloadManager.getMediaSource(VOD4))
        assertNull(preloadManager.getMediaSource(VOD5))

        assertTrue(preloadManager.remove(VOD2))
        assertTrue(preloadManager.remove(VOD3))
        preloadManager.invalidate()

        assertEquals(2, preloadManager.currentPlayingIndex)
        assertEquals(2, preloadManager.sourceCount)
        assertNotNull(preloadManager.getMediaSource(VOD1))
        assertNull(preloadManager.getMediaSource(VOD2))
        assertNull(preloadManager.getMediaSource(VOD3))
        assertNotNull(preloadManager.getMediaSource(VOD4))
        assertNull(preloadManager.getMediaSource(VOD5))

        preloadManager.reset()
        preloadManager.invalidate()

        assertEquals(2, preloadManager.currentPlayingIndex)
        assertEquals(0, preloadManager.sourceCount)
        assertNull(preloadManager.getMediaSource(VOD1))
        assertNull(preloadManager.getMediaSource(VOD2))
        assertNull(preloadManager.getMediaSource(VOD3))
        assertNull(preloadManager.getMediaSource(VOD4))
        assertNull(preloadManager.getMediaSource(VOD5))
    }

    private companion object {
        private val VOD1 = MediaItem.fromUri("urn:rts:video:13444390")
        private val VOD2 = MediaItem.fromUri("urn:rts:video:13444333")
        private val VOD3 = MediaItem.fromUri("urn:rts:video:13444466")
        private val VOD4 = MediaItem.fromUri("urn:rts:video:13444447")
        private val VOD5 = MediaItem.fromUri("urn:rts:video:13444352")
    }
}
