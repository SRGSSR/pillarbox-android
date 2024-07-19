/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.Player
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.analytics.metrics.SessionMetrics
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SessionMetricsTest {
    private lateinit var callback: (SessionMetrics) -> Unit

    @BeforeTest
    fun setUp() {
        callback = mockk<(SessionMetrics) -> Unit>(relaxed = true)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test loading time ready with DRM with buffering`() = runTest {
        val metricSession = SessionMetrics(timeProvider = { currentTime }, loadingTimeReady = callback)
        advanceTimeBy(100.seconds)
        metricSession.setPlaybackState(Player.STATE_BUFFERING)
        advanceTimeBy(1.seconds)
        metricSession.setIsPlaying(false)
        advanceTimeBy(1.seconds)
        metricSession.setDrmSessionAcquired()
        advanceTimeBy(1.seconds)
        metricSession.setPlaybackState(Player.STATE_READY)
        advanceTimeBy(1.seconds)
        metricSession.setIsPlaying(true)
        advanceTimeBy(1.seconds)
        metricSession.setDrmKeyLoaded()
        advanceTimeBy(10.seconds)

        verify(exactly = 1) {
            callback(metricSession)
        }
        confirmVerified(callback)

        assertEquals(5.seconds, metricSession.timeToReady)
        assertEquals(11.seconds, metricSession.totalPlayingDuration)
        assertEquals(3.seconds, metricSession.totalBufferingDuration)
    }

    @Test
    fun `test loading time ready with multi DRM key`() = runTest {
        val metricSession = SessionMetrics(timeProvider = { currentTime }, loadingTimeReady = callback)
        advanceTimeBy(100.seconds)
        metricSession.setPlaybackState(Player.STATE_BUFFERING)
        advanceTimeBy(1.seconds)
        metricSession.setDrmSessionAcquired()
        metricSession.setDrmSessionAcquired()
        advanceTimeBy(1.seconds)
        metricSession.setDrmKeyLoaded()
        advanceTimeBy(1.seconds)
        metricSession.setPlaybackState(Player.STATE_READY)
        advanceTimeBy(1.seconds)
        metricSession.setDrmKeyLoaded()
        advanceTimeBy(10.seconds)

        verify(exactly = 1) {
            callback(metricSession)
        }
        confirmVerified(callback)

        assertEquals(4.seconds, metricSession.timeToReady)
    }

    @Test
    fun `test loading time ready with DRM without buffering`() = runTest {
        val metricSession = SessionMetrics(timeProvider = { currentTime }, loadingTimeReady = callback)
        advanceTimeBy(100.seconds)
        metricSession.setPlaybackState(Player.STATE_READY)
        advanceTimeBy(1.seconds)
        metricSession.setIsPlaying(true)
        advanceTimeBy(1.seconds)
        metricSession.setDrmSessionAcquired()
        advanceTimeBy(1.seconds)
        metricSession.setDrmKeyLoaded()
        advanceTimeBy(10.seconds)

        verify(exactly = 1) {
            callback(metricSession)
        }
        confirmVerified(callback)

        assertNull(metricSession.timeToReady)
        assertEquals(12.seconds, metricSession.totalPlayingDuration)
        assertEquals(0.seconds, metricSession.totalBufferingDuration)
    }

    @Test
    fun `test loading time ready without DRM with buffering`() = runTest {
        val metricSession = SessionMetrics(timeProvider = { currentTime }, loadingTimeReady = callback)
        advanceTimeBy(100.seconds)
        metricSession.setPlaybackState(Player.STATE_BUFFERING)
        advanceTimeBy(1.seconds)
        metricSession.setIsPlaying(false)
        advanceTimeBy(1.seconds)
        metricSession.setPlaybackState(Player.STATE_READY)
        advanceTimeBy(1.seconds)
        metricSession.setIsPlaying(true)
        advanceTimeBy(1.seconds)

        verify(exactly = 1) {
            callback(metricSession)
        }
        confirmVerified(callback)

        assertEquals(2.seconds, metricSession.timeToReady)
        assertEquals(1.seconds, metricSession.totalPlayingDuration)
        assertEquals(2.seconds, metricSession.totalBufferingDuration)
    }

    @Test
    fun `test stall count and duration`() = runTest {
        val metricSession = SessionMetrics(timeProvider = { currentTime }, loadingTimeReady = callback)
        advanceTimeBy(100.seconds)
        metricSession.setIsStall(true)
        advanceTimeBy(1.seconds)
        metricSession.setIsStall(false)
        advanceTimeBy(10.seconds)
        metricSession.setIsStall(true)
        advanceTimeBy(1.seconds)
        metricSession.setIsStall(false)
        advanceTimeBy(10.seconds)

        assertEquals(2, metricSession.stallCount)
        assertEquals(2.seconds, metricSession.totalStallDuration)
    }
}
