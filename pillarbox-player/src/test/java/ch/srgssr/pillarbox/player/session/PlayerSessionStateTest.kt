/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PlayerSessionStateTest {
    @Test
    fun `create with bundle`() {
        val bundle = Bundle().apply {
            putBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG, true)
            putBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG, false)
        }
        val sessionState = PlayerSessionState(bundle)

        assertTrue(sessionState.smoothSeekingEnabled)
        assertFalse(sessionState.trackingEnabled)
    }

    @Test
    fun `create with empty bundle`() {
        val bundle = Bundle.EMPTY
        val sessionState = PlayerSessionState(bundle)

        assertFalse(sessionState.smoothSeekingEnabled)
        assertFalse(sessionState.trackingEnabled)
    }

    @Test
    fun `create with Pillarbox player`() {
        val player = PillarboxExoPlayer().apply {
            smoothSeekingEnabled = false
            trackingEnabled = true
        }
        val sessionState = PlayerSessionState(player)

        assertFalse(sessionState.smoothSeekingEnabled)
        assertTrue(sessionState.trackingEnabled)
    }

    @Test
    fun `to bundle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val player = PillarboxExoPlayer(context).apply {
            smoothSeekingEnabled = false
            trackingEnabled = true
        }
        val sessionState = PlayerSessionState(player)

        val bundle = sessionState.toBundle()

        assertEquals(2, bundle.size())
        assertFalse(bundle.getBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG))
        assertTrue(bundle.getBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG))
    }

    @Test
    fun `to bundle with extra data`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val player = PillarboxExoPlayer(context).apply {
            smoothSeekingEnabled = false
            trackingEnabled = true
        }
        val sessionState = PlayerSessionState(player)

        val extraData = Bundle().apply {
            putString("foo", "bar")
        }
        val bundle = sessionState.toBundle(extraData)

        assertEquals(extraData.size() + 2, bundle.size())
        assertEquals(extraData.getString("foo"), bundle.getString("foo"))
        assertFalse(bundle.getBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG))
        assertTrue(bundle.getBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG))
    }
}
