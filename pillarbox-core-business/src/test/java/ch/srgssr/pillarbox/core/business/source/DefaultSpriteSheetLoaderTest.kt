/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class DefaultSpriteSheetLoaderTest {

    @Test
    fun `malformed image url`() = runTest {
        val spriteSheet = SpriteSheet(
            urn = "urn:123",
            rows = 10,
            columns = 10,
            thumbnailHeight = 10,
            thumbnailWidth = 10,
            interval = 10,
            url = "not_valid url"
        )

        val result = SpriteSheetLoader.Default.loadSpriteSheet(spriteSheet)
        assertTrue(result.isFailure)
    }

    @Test
    fun `image url returns http 404`() = runTest {
        val spriteSheet = SpriteSheet(
            urn = "urn:123",
            rows = 10,
            columns = 10,
            thumbnailHeight = 10,
            thumbnailWidth = 10,
            interval = 10,
            url = "https://www.server.com/noimage.png"
        )

        val result = SpriteSheetLoader.Default.loadSpriteSheet(spriteSheet)
        assertTrue(result.isFailure)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `valid image url`() = runTest {
        val spriteSheet = SpriteSheet(
            urn = "urn:123",
            rows = 10,
            columns = 10,
            thumbnailHeight = 10,
            thumbnailWidth = 10,
            interval = 10,
            url = "https://il.srgssr.ch/spritesheet/urn/srf/video/881be9c2-65ec-4fa9-ba4a-926d15d046ef/sprite-881be9c2-65ec-4fa9-ba4a-926d15d046ef.jpeg"
        )

        val result = SpriteSheetLoader.Default.loadSpriteSheet(spriteSheet)
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
}
