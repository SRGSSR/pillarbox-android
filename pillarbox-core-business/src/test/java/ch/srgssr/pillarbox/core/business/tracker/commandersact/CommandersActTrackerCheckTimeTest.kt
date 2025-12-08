/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import android.content.Context
import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Pause
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Play
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Pos
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Seek
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Stop
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Uptime
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.core.business.utils.LocalMediaCompositionWithFallbackService
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * CommandersActTracker flaky tests
 */
@RunWith(AndroidJUnit4::class)
class CommandersActTrackerCheckTimeTest {
    @get:Rule
    val mockkRule = MockKRule(this)
    private lateinit var clock: FakeClock

    @MockK(relaxed = true)
    private lateinit var commandersAct: CommandersAct

    @MockK(relaxed = true)
    private lateinit var comscoreFactory: MediaItemTracker.Factory<ComScoreTracker.Data>

    private lateinit var testDispatcher: TestDispatcher

    private fun createPlayer(): ExoPlayer {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return PillarboxExoPlayer {
            clock(clock)
            srgAssetLoader(context) {
                mediaCompositionService(LocalMediaCompositionWithFallbackService(context))
                commanderActTrackerFactory(CommandersActTracker.Factory(commandersAct = commandersAct, coroutineContext = testDispatcher))
                comscoreTrackerFactory(comscoreFactory)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        clock = FakeClock(true)
    }

    @AfterTest
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `check uptime and position updates for live`() = runTest(testDispatcher) {
        val player = createPlayer()
        val playTime = 10.seconds
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 1.seconds
        CommandersActStreaming.POS_PERIOD = 2.seconds
        CommandersActStreaming.UPTIME_PERIOD = 4.seconds

        player.setMediaItem(SRGMediaItem(URN_LIVE_DVR_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        clock.advanceTime(playTime.inWholeMilliseconds)
        advanceTimeBy(playTime)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        player.playWhenReady = false

        TestPlayerRunHelper.runUntilPlayWhenReady(player, false)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        // Advance a bit more in time to ensure that no events are sent after pause
        clock.advanceTime(playTime.inWholeMilliseconds)
        advanceTimeBy(playTime)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        assertTrue(player.isCurrentMediaItemLive)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(10, tcMediaEvents.size)

        assertEquals(listOf(Pause, Pos, Uptime, Pos, Pos, Uptime, Pos, Uptime, Pos, Play), tcMediaEvents.map { it.eventType })
        assertTrue(tcMediaEvents.all { it.assets.isNotEmpty() })
        assertTrue(tcMediaEvents.all { it.sourceId == null })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `check uptime and position updates for dvr with time shift`() = runTest(testDispatcher) {
        val player = createPlayer()
        val playTime = 5.seconds
        val seekPosition = 80.seconds
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 1.seconds
        CommandersActStreaming.POS_PERIOD = 2.seconds
        CommandersActStreaming.UPTIME_PERIOD = 4.seconds

        player.setMediaItem(SRGMediaItem(URN_LIVE_DVR_AUDIO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        player.seekTo(seekPosition.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        clock.advanceTime(playTime.inWholeMilliseconds)
        advanceTimeBy(playTime)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        player.stop()

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(7, tcMediaEvents.size)

        assertEquals(listOf(Stop, Pos, Uptime, Pos, Play, Seek, Play), tcMediaEvents.map { it.eventType })
        assertTrue(tcMediaEvents.all { it.assets.isNotEmpty() })
        assertTrue(tcMediaEvents.all { it.sourceId == null })

        val timeShift = (player.duration.milliseconds - seekPosition).inWholeSeconds
        val actualTimeShift = tcMediaEvents.first {
            it.eventType == Pos || it.eventType == Uptime
        }.timeShift?.inWholeSeconds ?: 0L

        assertTrue(abs(timeShift - actualTimeShift) <= 15L, "Expected time shift to be <$timeShift>, but was <$actualTimeShift>")
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `check uptime and position updates for not live`() = runTest(testDispatcher) {
        val player = createPlayer()
        val playTime = 10.seconds
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 1.seconds
        CommandersActStreaming.POS_PERIOD = 2.seconds
        CommandersActStreaming.UPTIME_PERIOD = 4.seconds

        player.setMediaItem(SRGMediaItem(URN_NOT_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.playUntilStartOfMediaItem(player, 0)

        clock.advanceTime(playTime.inWholeMilliseconds)
        advanceTimeBy(playTime)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        player.playWhenReady = false

        TestPlayerRunHelper.runUntilPlayWhenReady(player, false)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        // Advance a bit more in time to ensure that no events are sent after pause
        clock.advanceTime(playTime.inWholeMilliseconds)
        advanceTimeBy(playTime)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        assertFalse(player.isCurrentMediaItemLive)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(7, tcMediaEvents.size)

        assertEquals(listOf(Pause, Pos, Pos, Pos, Pos, Pos, Play), tcMediaEvents.map { it.eventType })
        assertTrue(tcMediaEvents.all { it.assets.isNotEmpty() })
        assertTrue(tcMediaEvents.all { it.sourceId == null })
    }

    companion object {
        private const val URN_LIVE_DVR_AUDIO = LocalMediaCompositionWithFallbackService.URN_LIVE_DVR_AUDIO
        private const val URN_LIVE_DVR_VIDEO = LocalMediaCompositionWithFallbackService.URN_LIVE_DVR_VIDEO_TV
        private const val URN_NOT_LIVE_VIDEO = LocalMediaCompositionWithFallbackService.URN_VOD
    }
}
