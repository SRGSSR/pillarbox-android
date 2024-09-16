/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring

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
import ch.srgssr.pillarbox.player.monitoring.models.Message
import ch.srgssr.pillarbox.player.monitoring.models.Message.EventName
import ch.srgssr.pillarbox.player.monitoring.models.Timings
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MonitoringTest {
    private lateinit var player: PillarboxExoPlayer
    private lateinit var fakeClock: FakeClock
    private lateinit var monitoring: Monitoring
    private lateinit var monitoringMessageHandler: MonitoringMessageHandler

    @BeforeTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeClock = FakeClock(true)
        player = PillarboxExoPlayer(
            context = context,
            seekIncrement = SeekIncrement(),
            loadControl = DefaultLoadControl(),
            clock = fakeClock,
            coroutineContext = EmptyCoroutineContext,
            mediaSourceFactory = PillarboxMediaSourceFactory(context),
        )
        // Should be an input of ExoPlayer at least for test
        val metricsCollector = MetricsCollector()
        metricsCollector.setPlayer(player)
        monitoringMessageHandler = mockk(relaxed = true)
        monitoring = Monitoring(
            context = context,
            player = player,
            metricsCollector = metricsCollector,
            sessionManager = player.sessionManager,
            messageHandler = monitoringMessageHandler,
            coroutineContext = EmptyCoroutineContext,
        )
        player.prepare()
        player.play()
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `single item to end`() {
        player.setMediaItem(MediaItem.fromUri(VOD1))

        TestPlayerRunHelper.playUntilPosition(player, 0, 10L)

        val qoeTimings = monitoring.getCurrentQoETimings()
        val qosTimings = monitoring.getCurrentQoSTimings()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)
        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val messages = mutableListOf<Message>()

        verify {
            monitoringMessageHandler.sendEvent(capture(messages))
        }
        confirmVerified(monitoringMessageHandler)

        assertEquals(listOf(EventName.START, EventName.HEARTBEAT, EventName.STOP), messages.map { it.eventName })
        assertEquals(1, messages.distinctBy { it.sessionId }.count())

        assertEquals(Timings.QoS(), qosTimings)
        assertNotNull(qoeTimings)
        assertNotNull(qoeTimings.total)
        assertTrue(qoeTimings.total != 0L)
    }

    @Test
    fun `multiple items to end`() {
        player.setMediaItems(
            listOf(
                MediaItem.fromUri(VOD1),
                MediaItem.fromUri(VOD2)
            )
        )

        TestPlayerRunHelper.playUntilPosition(player, 0, 10L)

        val qoeTimings1 = monitoring.getCurrentQoETimings()
        val qosTimings1 = monitoring.getCurrentQoSTimings()

        TestPlayerRunHelper.runUntilTimelineChanged(player)
        TestPlayerRunHelper.playUntilPosition(player, 1, 10L)

        val qoeTimings2 = monitoring.getCurrentQoETimings()
        val qosTimings2 = monitoring.getCurrentQoSTimings()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)
        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val messages = mutableListOf<Message>()

        verify {
            monitoringMessageHandler.sendEvent(capture(messages))
        }
        confirmVerified(monitoringMessageHandler)

        assertEquals(
            listOf(
                listOf(EventName.START, EventName.HEARTBEAT, EventName.STOP),
                listOf(EventName.START, EventName.HEARTBEAT, EventName.STOP)
            ),
            messages.groupBy { it.sessionId }.map { it.value.map { it.eventName } }
        )
        assertEquals(2, messages.distinctBy { it.sessionId }.count())

        assertNotSame(qosTimings1, qosTimings2)
        assertNotSame(qoeTimings1, qoeTimings2)

        assertEquals(Timings.QoS(), qosTimings1)
        assertNotNull(qoeTimings1)
        assertNotNull(qoeTimings1.total)
        assertTrue(qoeTimings1.total != 0L)

        assertEquals(Timings.QoS(), qosTimings2)
        assertNotNull(qoeTimings2)
        assertNotNull(qoeTimings2.total)
        assertTrue(qoeTimings2.total != 0L)
    }

    @Test
    fun `multiple items with error`() {
        player.setMediaItems(
            listOf(
                MediaItem.fromUri("Https://error.m3u8"),
                MediaItem.fromUri(VOD2)
            )
        )

        TestPlayerRunHelper.run(player).untilPlayerError()
        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val messages = mutableListOf<Message>()

        verify {
            monitoringMessageHandler.sendEvent(capture(messages))
        }
        confirmVerified(monitoringMessageHandler)

        assertEquals(2, messages.size)
        assertEquals(listOf(EventName.START, EventName.ERROR), messages.map { it.eventName })
        assertEquals(1, messages.distinctBy { it.sessionId }.count())
    }

    private companion object {
        private const val VOD1 = "https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8"
        private const val VOD2 = "https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8"
    }
}
