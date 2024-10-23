/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.monitoring.models.Message
import ch.srgssr.pillarbox.player.monitoring.models.Message.EventName
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class MonitoringTest {
    private lateinit var player: PillarboxExoPlayer
    private lateinit var monitoringMessageHandler: MonitoringMessageHandler

    @BeforeTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        monitoringMessageHandler = mockk(relaxed = true)
        player = PillarboxExoPlayer(context) {
            clock(FakeClock(true))
            coroutineContext(UnconfinedTestDispatcher())
            monitoring = monitoringMessageHandler
        }
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

        TestPlayerRunHelper.playUntilPosition(player, 0, 5.seconds.inWholeMilliseconds)

        val qoeTimings = player.monitoring.getCurrentQoETimings()
        val qosTimings = player.monitoring.getCurrentQoSTimings()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)
        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val messages = mutableListOf<Message>()

        verify(exactly = 3) {
            monitoringMessageHandler.sendEvent(capture(messages))
        }
        confirmVerified(monitoringMessageHandler)

        assertEquals(listOf(EventName.START, EventName.HEARTBEAT, EventName.STOP), messages.map { it.eventName })
        assertEquals(1, messages.distinctBy { it.sessionId }.count())

        assertNotNull(qosTimings)
        assertNotNull(qoeTimings)
    }

    @Test
    fun `multiple items to end`() {
        player.setMediaItems(
            listOf(
                MediaItem.fromUri(VOD1),
                MediaItem.fromUri(VOD2)
            )
        )

        TestPlayerRunHelper.playUntilPosition(player, 0, 5.seconds.inWholeMilliseconds)

        val qoeTimings1 = player.monitoring.getCurrentQoETimings()
        val qosTimings1 = player.monitoring.getCurrentQoSTimings()

        TestPlayerRunHelper.runUntilTimelineChanged(player)
        TestPlayerRunHelper.playUntilPosition(player, 1, 5.seconds.inWholeMilliseconds)

        val qoeTimings2 = player.monitoring.getCurrentQoETimings()
        val qosTimings2 = player.monitoring.getCurrentQoSTimings()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)
        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val messages = mutableListOf<Message>()

        verify(exactly = 6) {
            monitoringMessageHandler.sendEvent(capture(messages))
        }
        confirmVerified(monitoringMessageHandler)

        val messagesBySessionId = messages.groupBy { it.sessionId }

        assertEquals(
            listOf(
                listOf(EventName.START, EventName.HEARTBEAT, EventName.STOP),
                listOf(EventName.START, EventName.HEARTBEAT, EventName.STOP)
            ),
            messagesBySessionId.map { entry -> entry.value.map { it.eventName } }
        )
        assertEquals(2, messagesBySessionId.size)

        assertNotSame(qosTimings1, qosTimings2)
        assertNotSame(qoeTimings1, qoeTimings2)
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

        verify(exactly = 2) {
            monitoringMessageHandler.sendEvent(capture(messages))
        }
        confirmVerified(monitoringMessageHandler)

        assertEquals(listOf(EventName.START, EventName.ERROR), messages.map { it.eventName })
        assertEquals(1, messages.distinctBy { it.sessionId }.count())
    }

    private companion object {
        private const val VOD1 = "https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8"
        private const val VOD2 = "https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8"
    }
}
