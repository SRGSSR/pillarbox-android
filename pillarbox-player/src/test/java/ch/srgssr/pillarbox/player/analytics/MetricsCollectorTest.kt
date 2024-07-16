/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.analytics.metrics.MetricsCollector
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration

@RunWith(AndroidJUnit4::class)
class MetricsCollectorTest {

    private lateinit var player: PillarboxExoPlayer
    private lateinit var metricsCollector: MetricsCollector
    private lateinit var fakeClock: FakeClock
    private lateinit var metricsListener: MetricsCollector.Listener

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        metricsListener = mockk(relaxed = true)
        fakeClock = FakeClock(true)
        metricsCollector = MetricsCollector()
        player = PillarboxExoPlayer(
            context = context,
            seekIncrement = SeekIncrement(),
            loadControl = DefaultLoadControl(),
            clock = fakeClock,
            coroutineContext = EmptyCoroutineContext,
            mediaSourceFactory = PillarboxMediaSourceFactory(context),
            metricsCollector = metricsCollector
        )
        metricsCollector.addListener(metricsListener)
        player.prepare()
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        shadowOf(Looper.getMainLooper()).idle()
        player.release()
    }

    @Test
    fun `single item playback`() {
        player.setMediaItem(MEDIA_ITEM)
        player.play()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // Session is finished when starting another media or when there is no more current item
        player.clearMediaItems()
        player.stop()
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val slotReady = slot<PlaybackMetrics>()
        val slotFinished = slot<PlaybackMetrics>()
        verify {
            metricsListener.hashCode()
            metricsListener.onMetricSessionReady(capture(slotReady))
            metricsListener.onMetricSessionFinished(capture(slotFinished))
        }
        confirmVerified(metricsListener)

        assertTrue(slotReady.isCaptured)
        slotReady.captured.also {
            Assert.assertNotNull(it.loadDuration.source)
            Assert.assertNotNull(it.loadDuration.manifest)
            Assert.assertNotNull(it.loadDuration.timeToReady)
            Assert.assertNotNull(it.loadDuration.asset)
            Assert.assertNull(it.loadDuration.drm)
            assertEquals(Duration.ZERO, it.playbackDuration)
        }

        assertTrue(slotFinished.isCaptured)
        slotFinished.captured.also {
            Assert.assertNotNull(it.loadDuration.source)
            Assert.assertNotNull(it.loadDuration.manifest)
            Assert.assertNotNull(it.loadDuration.timeToReady)
            Assert.assertNotNull(it.loadDuration.asset)
            Assert.assertNull(it.loadDuration.drm)
            assertNotEquals(Duration.ZERO, it.playbackDuration)
        }
    }

    @Test
    fun `playback item transition`() {
        player.setMediaItems(listOf(MEDIA_ITEM, MEDIA_ITEM))

        TestPlayerRunHelper.playUntilStartOfMediaItem(player, 1)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // Session is finished when starting another media or when there is no more current item
        player.clearMediaItems()
        player.stop()
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val finishedMetrics = mutableListOf<PlaybackMetrics>()
        val startedMetrics = mutableListOf<PlaybackMetrics>()
        verify {
            metricsListener.hashCode()
            metricsListener.onMetricSessionReady(capture(startedMetrics))
            metricsListener.onMetricSessionFinished(capture(finishedMetrics))
        }
        confirmVerified(metricsListener)

        assertEquals(2, startedMetrics.size)
        assertNotEquals(startedMetrics[0].sessionId, startedMetrics[1].sessionId)

        assertEquals(2, finishedMetrics.size)
        assertNotEquals(finishedMetrics[0].sessionId, finishedMetrics[1].sessionId)
    }

    private companion object {
        val MEDIA_ITEM = MediaItem.fromUri("https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8")
    }
}
