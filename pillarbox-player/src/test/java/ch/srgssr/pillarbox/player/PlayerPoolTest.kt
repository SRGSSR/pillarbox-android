/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PlayerPoolTest {
    @Test(expected = IllegalArgumentException::class)
    fun `playerCount can not be negative`() {
        PlayerPool(
            playersCount = -1,
            playerFactory = { PillarboxExoPlayer(ApplicationProvider.getApplicationContext()) },
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `playerCount can not be 0`() {
        PlayerPool(
            playersCount = 0,
            playerFactory = { PillarboxExoPlayer(ApplicationProvider.getApplicationContext()) },
        )
    }

    @Test
    fun `get player at position`() {
        val playersCount = 5
        var createdPlayers = 0
        val pool = PlayerPool(
            playersCount = playersCount,
            playerFactory = {
                createdPlayers++

                PillarboxExoPlayer(ApplicationProvider.getApplicationContext())
            },
        )

        // Invalid position
        assertNull(pool.getPlayerAtPosition(-1))
        assertEquals(0, createdPlayers)

        // Position < playersCount
        assertNotNull(pool.getPlayerAtPosition(2))
        assertEquals(1, createdPlayers)

        // Position > playersCount
        val requestedPlayerOffset = 3
        assertNotNull(pool.getPlayerAtPosition(playersCount + requestedPlayerOffset))
        assertEquals(2, createdPlayers)

        // Reuse player instance
        assertEquals(pool.getPlayerAtPosition(requestedPlayerOffset), pool.getPlayerAtPosition(playersCount + requestedPlayerOffset))
        assertEquals(2, createdPlayers)
    }

    @Test
    fun release() {
        val pool = PlayerPool(
            playersCount = 3,
            playerFactory = { PillarboxExoPlayer(ApplicationProvider.getApplicationContext()) },
        )
        val player = pool.getPlayerAtPosition(1)

        assertNotNull(player)
        assertFalse(player.isReleased)

        pool.release()
        assertTrue(player.isReleased)
    }
}
