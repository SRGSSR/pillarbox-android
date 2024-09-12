/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.analytics.metrics.LoadingTimes
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LoadingTimesTest {
    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `buffering to ready call onLoadingReady`() = runTest {
        val callback = mockk<() -> Unit>(relaxed = true)
        val loadingTimes = LoadingTimes(onLoadingReady = callback, timeProvider = { currentTime })
        advanceTimeBy(10.seconds)

        loadingTimes.state = Player.STATE_BUFFERING
        advanceTimeBy(20.seconds)
        loadingTimes.state = Player.STATE_READY

        verify(exactly = 1) {
            callback()
        }
        confirmVerified(callback)
        val timeToReady = loadingTimes.timeToReady
        assertNotNull(timeToReady)
        assertEquals(20.seconds, timeToReady)
    }

    @Test
    fun `not ready don't call onLoadingReady`() = runTest {
        val callback = mockk<() -> Unit>(relaxed = true)
        val loadingTimes = LoadingTimes(onLoadingReady = callback, timeProvider = { currentTime })
        advanceTimeBy(10.seconds)

        loadingTimes.state = Player.STATE_BUFFERING
        advanceTimeBy(20.seconds)
        loadingTimes.state = Player.STATE_ENDED

        verify(exactly = 0) {
            callback()
        }
        confirmVerified(callback)
        assertNull(loadingTimes.timeToReady)
    }

    @Test
    fun `initialization to READY call onLoadingReady`() = runTest {
        val callback = mockk<() -> Unit>(relaxed = true)
        val loadingTimes = LoadingTimes(onLoadingReady = callback, timeProvider = { currentTime })

        advanceTimeBy(20.seconds)
        loadingTimes.state = Player.STATE_READY

        verify(exactly = 1) {
            callback()
        }
        confirmVerified(callback)
        val timeToReady = loadingTimes.timeToReady
        assertNull(timeToReady)
    }

    @Test
    fun `call twice ready call once onLoadingReady`() = runTest {
        val callback = mockk<() -> Unit>(relaxed = true)
        val loadingTimes = LoadingTimes(onLoadingReady = callback, timeProvider = { currentTime })

        loadingTimes.state = Player.STATE_BUFFERING
        advanceTimeBy(20.seconds)
        loadingTimes.state = Player.STATE_READY
        advanceTimeBy(5.seconds)
        loadingTimes.state = Player.STATE_READY

        verify(exactly = 1) {
            callback()
        }
        confirmVerified(callback)
        val timeToReady = loadingTimes.timeToReady
        assertNotNull(timeToReady)
        assertEquals(20.seconds, timeToReady)
    }

    @Test
    fun `twice buffering and ready keep only the first timeToReady`() = runTest {
        val callback = mockk<() -> Unit>(relaxed = true)
        val loadingTimes = LoadingTimes(onLoadingReady = callback, timeProvider = { currentTime })

        loadingTimes.state = Player.STATE_BUFFERING
        advanceTimeBy(20.seconds)
        loadingTimes.state = Player.STATE_READY

        advanceTimeBy(5.seconds)
        loadingTimes.state = Player.STATE_BUFFERING
        advanceTimeBy(5.seconds)
        loadingTimes.state = Player.STATE_READY

        verify(exactly = 1) {
            callback()
        }
        confirmVerified(callback)
        val timeToReady = loadingTimes.timeToReady
        assertNotNull(timeToReady)
        assertEquals(20.seconds, timeToReady)
    }
}
