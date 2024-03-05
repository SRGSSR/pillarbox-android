/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Eof
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Pause
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Play
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Pos
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Seek
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Stop
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType.Uptime
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.MediaItemUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultHttpClient
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.test.utils.TestPillarboxRunHelper
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CommandersActTrackerIntegrationTest {
    private lateinit var clock: FakeClock
    private lateinit var commandersAct: CommandersAct
    private lateinit var player: ExoPlayer
    private lateinit var testDispatcher: TestDispatcher

    @BeforeTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setup() {
        clock = FakeClock(true)
        commandersAct = mockk(relaxed = true)
        testDispatcher = UnconfinedTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val mediaItemTrackerRepository = DefaultMediaItemTrackerRepository(
            trackerRepository = MediaItemTrackerRepository(),
            commandersAct = commandersAct,
            coroutineContext = testDispatcher,
        )
        mediaItemTrackerRepository.registerFactory(ComScoreTracker::class.java) {
            mockk<ComScoreTracker>(relaxed = true)
        }

        val urnMediaItemSource = MediaCompositionMediaItemSource(
            mediaCompositionDataSource = LocalMediaCompositionWithFallbackDataSource(context)
        )
        val mediaItemSource = object : MediaItemSource {
            override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
                return if (mediaItem.mediaId.isValidMediaUrn()) {
                    urnMediaItemSource.loadMediaItem(mediaItem)
                } else {
                    mediaItem
                }
            }
        }

        player = DefaultPillarbox(
            context = context,
            mediaItemTrackerRepository = mediaItemTrackerRepository,
            mediaItemSource = mediaItemSource,
            clock = clock,
        )
    }

    @AfterTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tearDown() {
        player.release()

        shadowOf(Looper.getMainLooper()).idle()

        Dispatchers.resetMain()
    }

    @Test
    fun `player unprepared`() {
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verify { commandersAct wasNot Called }
    }

    @Test
    fun `player prepared and playing, changing media item`() {
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        player.setMediaItem(MediaItemUrn(URN_NOT_LIVE_VIDEO))
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(3, tcMediaEvents.size)

        assertEquals(Play, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(Stop, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(Play, tcMediaEvents[2].eventType)
        assertTrue(tcMediaEvents[2].assets.isNotEmpty())
        assertNull(tcMediaEvents[2].sourceId)
    }

    @Test
    fun `audio URN don't send any analytics`() {
        val tcMediaEventSlot = slot<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_AUDIO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEventSlot))
        }
        confirmVerified(commandersAct)

        val tcMediaEvent = tcMediaEventSlot.captured

        assertEquals(Play, tcMediaEvent.eventType)
        assertTrue(tcMediaEvent.assets.isNotEmpty())
        assertNull(tcMediaEvent.sourceId)
    }

    @Test
    fun `URL don't send any analytics`() {
        player.setMediaItem(MediaItem.fromUri(URL))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verify { commandersAct wasNot Called }
    }

    @Test
    fun `player prepared but not playing`() {
        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
        }
        confirmVerified(commandersAct)
    }

    @Test
    fun `player prepared and playing`() {
        val tcMediaEventSlot = slot<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEventSlot))
        }
        confirmVerified(commandersAct)

        val tcMediaEvent = tcMediaEventSlot.captured

        assertEquals(Play, tcMediaEvent.eventType)
        assertTrue(tcMediaEvent.assets.isNotEmpty())
        assertNull(tcMediaEvent.sourceId)
    }

    @Test
    fun `player prepared and playing, change playback speed`() {
        val tcMediaEventSlot = slot<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true
        player.setPlaybackSpeed(2f)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEventSlot))
        }
        confirmVerified(commandersAct)

        val tcMediaEvent = tcMediaEventSlot.captured

        assertEquals(Play, tcMediaEvent.eventType)
        assertTrue(tcMediaEvent.assets.isNotEmpty())
        assertNull(tcMediaEvent.sourceId)
    }

    @Test
    fun `player prepared and playing, change playback speed while playing`() {
        val tcMediaEventSlot = slot<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        clock.advanceTime(5.minutes.inWholeMilliseconds)
        player.setPlaybackSpeed(2f)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEventSlot))
        }
        confirmVerified(commandersAct)

        val tcMediaEvent = tcMediaEventSlot.captured

        assertEquals(Play, tcMediaEvent.eventType)
        assertTrue(tcMediaEvent.assets.isNotEmpty())
        assertNull(tcMediaEvent.sourceId)
    }

    @Test
    fun `player prepared, playing and paused`() {
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.playWhenReady = false

        TestPlayerRunHelper.runUntilPlayWhenReady(player, false)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(2, tcMediaEvents.size)

        assertEquals(Pause, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(Play, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)
    }

    @Test
    fun `player prepared, playing, paused, playing again`() {
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.playWhenReady = false

        TestPlayerRunHelper.runUntilPlayWhenReady(player, false)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        clock.advanceTime(4.minutes.inWholeMilliseconds)
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlayWhenReady(player, true)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(3, tcMediaEvents.size)

        assertEquals(Play, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(Pause, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(Play, tcMediaEvents[2].eventType)
        assertTrue(tcMediaEvents[2].assets.isNotEmpty())
        assertNull(tcMediaEvents[2].sourceId)
    }

    @Test
    fun `player prepared, playing and stopped`() {
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.stop()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(2, tcMediaEvents.size)

        assertEquals(Stop, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(Play, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)
    }

    @Test
    fun `player prepared, playing and seeking`() {
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_NOT_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.seekTo(30.seconds.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(3, tcMediaEvents.size)

        assertEquals(Play, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(Seek, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(Play, tcMediaEvents[2].eventType)
        assertTrue(tcMediaEvents[2].assets.isNotEmpty())
        assertNull(tcMediaEvents[2].sourceId)
    }

    @Test
    fun `player pause, playing, seeking and playing`() {
        val tcMediaEventSlot = slot<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_NOT_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = false

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.play()
        player.seekTo(30.seconds.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEventSlot))
        }
        confirmVerified(commandersAct)

        val tcMediaEvent = tcMediaEventSlot.captured

        assertEquals(Play, tcMediaEvent.eventType)
        assertTrue(tcMediaEvent.assets.isNotEmpty())
        assertNull(tcMediaEvent.sourceId)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `player playing, pause, seeking and pause`() = runTest(testDispatcher) {
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        player.setMediaItem(MediaItemUrn(URN_NOT_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        clock.advanceTime(2.seconds.inWholeMilliseconds)
        advanceTimeBy(2.seconds)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        player.pause()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPlayWhenReady(player, false)

        clock.advanceTime(2.seconds.inWholeMilliseconds)
        advanceTimeBy(2.seconds)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        player.seekTo(30.seconds.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(3, tcMediaEvents.size)

        assertEquals(Pause, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(Pos, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(Play, tcMediaEvents[2].eventType)
        assertTrue(tcMediaEvents[2].assets.isNotEmpty())
        assertNull(tcMediaEvents[2].sourceId)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `player pause, seeking and pause`() = runTest(testDispatcher) {
        player.setMediaItem(MediaItemUrn(URN_NOT_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = false

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.seconds.inWholeMilliseconds)
        advanceTimeBy(2.seconds)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        player.seekTo(30.seconds.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            commandersAct.enableRunningInBackground()
        }
        confirmVerified(commandersAct)
    }

    @Test
    fun `player prepared and seek`() {
        player.setMediaItem(MediaItemUrn(URN_NOT_LIVE_VIDEO))
        player.prepare()
        player.seekTo(3.minutes.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            commandersAct.enableRunningInBackground()
        }
        confirmVerified(commandersAct)
    }

    @Test
    fun `player prepared and stopped`() {
        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.stop()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verify { commandersAct wasNot Called }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `check uptime and position updates for live`() = runTest(testDispatcher) {
        val playTime = 10.seconds
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 1.seconds
        CommandersActStreaming.POS_PERIOD = 2.seconds
        CommandersActStreaming.UPTIME_PERIOD = 4.seconds

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

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
        val playTime = 5.seconds
        val seekPosition = 80.seconds
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 1.seconds
        CommandersActStreaming.POS_PERIOD = 2.seconds
        CommandersActStreaming.UPTIME_PERIOD = 4.seconds

        player.setMediaItem(MediaItemUrn(URN_DVR))
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
        val playTime = 10.seconds
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 1.seconds
        CommandersActStreaming.POS_PERIOD = 2.seconds
        CommandersActStreaming.UPTIME_PERIOD = 4.seconds

        player.setMediaItem(MediaItemUrn(URN_NOT_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

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

    @Test
    fun `start EoF`() = runTest(testDispatcher) {
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 1.seconds
        CommandersActStreaming.POS_PERIOD = 2.seconds
        CommandersActStreaming.UPTIME_PERIOD = 4.seconds

        player.setMediaItem(MediaItemUrn(URN_VOD_SHORT))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(2, tcMediaEvents.size)

        assertEquals(listOf(Eof, Play), tcMediaEvents.map { it.eventType })
        assertTrue(tcMediaEvents.all { it.assets.isNotEmpty() })
        assertTrue(tcMediaEvents.all { it.sourceId == null })
    }

    private class LocalMediaCompositionWithFallbackDataSource(
        context: Context,
        private val fallbackDataSource: MediaCompositionDataSource = DefaultMediaCompositionDataSource(),
    ) : MediaCompositionDataSource {
        private var mediaComposition: MediaComposition? = null

        init {
            val json = context.assets.open("media-composition.json").bufferedReader().use { it.readText() }

            mediaComposition = DefaultHttpClient.jsonSerializer.decodeFromString(json)
        }

        override suspend fun getMediaCompositionByUrn(urn: String): Result<MediaComposition> {
            return if (urn == URN_DVR) {
                runCatching {
                    requireNotNull(mediaComposition)
                }
            } else {
                fallbackDataSource.getMediaCompositionByUrn(urn)
            }
        }
    }

    private companion object {
        private const val URL = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"
        private const val URN_AUDIO = "urn:rts:audio:13598743"
        private const val URN_LIVE_VIDEO = "urn:rts:video:8841634"
        private const val URN_NOT_LIVE_VIDEO = "urn:rsi:video:15916771"
        private const val URN_VOD_SHORT = "urn:rts:video:13444428"
        private const val URN_DVR = "urn:rts:audio:3262363"
    }
}
