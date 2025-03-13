/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.cast.SessionAvailabilityListener
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CastExtensionsTest {
    @Test
    fun `is connected, state unknown`() {
        val castContext = mockk<CastContext> {
            every { castState } returns 0
        }

        assertFalse(castContext.isConnected())
    }

    @Test
    fun `is connected, state no devices available`() {
        val castContext = mockk<CastContext> {
            every { castState } returns CastState.NO_DEVICES_AVAILABLE
        }

        assertFalse(castContext.isConnected())
    }

    @Test
    fun `is connected, state not connected`() {
        val castContext = mockk<CastContext> {
            every { castState } returns CastState.NOT_CONNECTED
        }

        assertFalse(castContext.isConnected())
    }

    @Test
    fun `is connected, state connecting`() {
        val castContext = mockk<CastContext> {
            every { castState } returns CastState.CONNECTING
        }

        assertTrue(castContext.isConnected())
    }

    @Test
    fun `is connected, state connected`() {
        val castContext = mockk<CastContext> {
            every { castState } returns CastState.CONNECTED
        }

        assertTrue(castContext.isConnected())
    }

    @Test
    fun `is cast session available as flow`() = runTest {
        val listenerSlot = slot<SessionAvailabilityListener>()
        val castPlayer = mockk<PillarboxCastPlayer> {
            every { isCastSessionAvailable() } returns false
            justRun { setSessionAvailabilityListener(capture(listenerSlot)) }
        }

        castPlayer.isCastSessionAvailableAsFlow().test {
            val listener = listenerSlot.captured

            assertFalse(awaitItem())

            listener.onCastSessionAvailable()
            assertTrue(awaitItem())

            listener.onCastSessionUnavailable()
            assertFalse(awaitItem())
        }
    }
}
