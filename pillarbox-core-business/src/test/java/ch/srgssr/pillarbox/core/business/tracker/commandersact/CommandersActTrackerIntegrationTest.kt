/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.MediaItemUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
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
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CommandersActTrackerIntegrationTest {
    private lateinit var clock: FakeClock
    private lateinit var commandersAct: CommandersAct
    private lateinit var player: ExoPlayer

    @BeforeTest
    fun setup() {
        clock = FakeClock(true)
        commandersAct = mockk(relaxed = true)

        val mediaItemTrackerRepository = DefaultMediaItemTrackerRepository(
            trackerRepository = MediaItemTrackerRepository(),
            commandersAct = commandersAct,
        )
        mediaItemTrackerRepository.registerFactory(ComScoreTracker::class.java) {
            mockk<ComScoreTracker>(relaxed = true)
        }

        val urnMediaItemSource = MediaCompositionMediaItemSource(
            mediaCompositionDataSource = DefaultMediaCompositionDataSource()
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
            context = ApplicationProvider.getApplicationContext(),
            mediaItemTrackerRepository = mediaItemTrackerRepository,
            mediaItemSource = mediaItemSource,
            clock = clock,
        )
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
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        verifyOrder {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(capture(tcMediaEvents))
        }
        confirmVerified(commandersAct)

        assertEquals(3, tcMediaEvents.size)

        assertEquals(MediaEventType.Play, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(MediaEventType.Stop, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(MediaEventType.Play, tcMediaEvents[2].eventType)
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

        assertEquals(MediaEventType.Play, tcMediaEvent.eventType)
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

        assertEquals(MediaEventType.Play, tcMediaEvent.eventType)
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

        assertEquals(MediaEventType.Play, tcMediaEvent.eventType)
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

        assertEquals(MediaEventType.Play, tcMediaEvent.eventType)
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

        assertEquals(MediaEventType.Pause, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(MediaEventType.Play, tcMediaEvents[1].eventType)
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

        assertEquals(MediaEventType.Play, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(MediaEventType.Pause, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(MediaEventType.Play, tcMediaEvents[2].eventType)
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

        assertEquals(MediaEventType.Stop, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(MediaEventType.Play, tcMediaEvents[1].eventType)
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

        assertEquals(MediaEventType.Play, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(MediaEventType.Seek, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(MediaEventType.Play, tcMediaEvents[2].eventType)
        assertTrue(tcMediaEvents[2].assets.isNotEmpty())
        assertNull(tcMediaEvents[2].sourceId)
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
    fun `check uptime and position updates`() {
        val delay = 2.seconds
        val tcMediaEvents = mutableListOf<TCMediaEvent>()

        CommandersActStreaming.HEART_BEAT_DELAY = 0.5.seconds
        CommandersActStreaming.POS_PERIOD = 0.5.seconds
        CommandersActStreaming.UPTIME_PERIOD = 1.seconds

        player.setMediaItem(MediaItemUrn(URN_LIVE_VIDEO))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPillarboxRunHelper.runUntilStartOfMediaItem(player, 0)

        clock.advanceTime(delay.inWholeMilliseconds)
        Thread.sleep(delay.inWholeMilliseconds)
        player.playWhenReady = false

        TestPlayerRunHelper.runUntilPlayWhenReady(player, false)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

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
        }
        confirmVerified(commandersAct)

        assertEquals(8, tcMediaEvents.size)

        assertEquals(MediaEventType.Pause, tcMediaEvents[0].eventType)
        assertTrue(tcMediaEvents[0].assets.isNotEmpty())
        assertNull(tcMediaEvents[0].sourceId)

        assertEquals(MediaEventType.Pos, tcMediaEvents[1].eventType)
        assertTrue(tcMediaEvents[1].assets.isNotEmpty())
        assertNull(tcMediaEvents[1].sourceId)

        assertEquals(MediaEventType.Uptime, tcMediaEvents[2].eventType)
        assertTrue(tcMediaEvents[2].assets.isNotEmpty())
        assertNull(tcMediaEvents[2].sourceId)

        assertEquals(MediaEventType.Pos, tcMediaEvents[3].eventType)
        assertTrue(tcMediaEvents[3].assets.isNotEmpty())
        assertNull(tcMediaEvents[3].sourceId)

        assertEquals(MediaEventType.Pos, tcMediaEvents[4].eventType)
        assertTrue(tcMediaEvents[4].assets.isNotEmpty())
        assertNull(tcMediaEvents[4].sourceId)

        assertEquals(MediaEventType.Uptime, tcMediaEvents[5].eventType)
        assertTrue(tcMediaEvents[5].assets.isNotEmpty())
        assertNull(tcMediaEvents[5].sourceId)

        assertEquals(MediaEventType.Pos, tcMediaEvents[6].eventType)
        assertTrue(tcMediaEvents[6].assets.isNotEmpty())
        assertNull(tcMediaEvents[6].sourceId)

        assertEquals(MediaEventType.Play, tcMediaEvents[7].eventType)
        assertTrue(tcMediaEvents[7].assets.isNotEmpty())
        assertNull(tcMediaEvents[7].sourceId)
    }

    private companion object {
        private const val URL = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"
        private const val URN_AUDIO = "urn:rts:audio:13598743"
        private const val URN_LIVE_VIDEO = "urn:rts:video:8841634"
        private const val URN_NOT_LIVE_VIDEO = "urn:rsi:video:15916771"
    }
}
