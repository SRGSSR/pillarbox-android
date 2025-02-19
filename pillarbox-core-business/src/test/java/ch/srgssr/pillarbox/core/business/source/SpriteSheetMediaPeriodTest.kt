/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import io.mockk.clearAllMocks
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SpriteSheetMediaPeriodTest {
    private val spriteSheetLoader = SpriteSheetLoader.Default
    private lateinit var testDispatcher: TestDispatcher

    @BeforeTest
    fun init() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun release() {
        Dispatchers.resetMain()
        shadowOf(Looper.getMainLooper()).idle()
        clearAllMocks()
    }

    @Test
    fun `prepare doesn't crash with malformed url`() = runTest(testDispatcher) {
        val spriteSheet = SpriteSheet(
            urn = "urn:123",
            rows = 10,
            columns = 10,
            thumbnailHeight = 10,
            thumbnailWidth = 10,
            interval = 10,
            url = "not_valid url"
        )

        val mediaPeriod = SpriteSheetMediaPeriod(spriteSheet = spriteSheet, spriteSheetLoader = spriteSheetLoader, testDispatcher)
        mediaPeriod.prepare(mockk(relaxed = true), 1L)
        advanceUntilIdle()

        assertFalse(mediaPeriod.isLoading)
        assertNull(mediaPeriod.bitmap)
    }

    @Test
    fun `prepare doesn't crash with image url producing 404 error`() = runTest(testDispatcher) {
        val spriteSheet = SpriteSheet(
            urn = "urn:123",
            rows = 10,
            columns = 10,
            thumbnailHeight = 10,
            thumbnailWidth = 10,
            interval = 10,
            url = "https://www.serveur.com/noimage.png"
        )

        val mediaPeriod = SpriteSheetMediaPeriod(spriteSheet = spriteSheet, spriteSheetLoader = spriteSheetLoader, testDispatcher)
        mediaPeriod.prepare(mockk(relaxed = true), 1L)
        advanceUntilIdle()

        assertFalse(mediaPeriod.isLoading)
        assertNull(mediaPeriod.bitmap)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `prepare loads image`() = runTest(testDispatcher) {
        val spriteSheet = SpriteSheet(
            urn = "urn:123",
            rows = 10,
            columns = 10,
            thumbnailHeight = 10,
            thumbnailWidth = 10,
            interval = 10,
            url = "https://il.srgssr.ch/spritesheet/urn/srf/video/881be9c2-65ec-4fa9-ba4a-926d15d046ef/sprite-881be9c2-65ec-4fa9-ba4a-926d15d046ef.jpeg"
        )

        val mediaPeriod = SpriteSheetMediaPeriod(spriteSheet = spriteSheet, spriteSheetLoader = spriteSheetLoader, testDispatcher)
        mediaPeriod.prepare(mockk(relaxed = true), 1L)
        advanceUntilIdle()

        assertFalse(mediaPeriod.isLoading)
        assertNotNull(mediaPeriod.bitmap)
    }
}
