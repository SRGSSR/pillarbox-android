/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import io.mockk.every
import io.mockk.mockk
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
}
