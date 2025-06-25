/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import ch.srgssr.pillarbox.player.test.utils.TestPillarboxRunHelper
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class PlayerCallbackFlowTest {
    private lateinit var player: ExoPlayer

    @BeforeTest
    fun setUp() {
        player = PillarboxExoPlayer().apply {
            prepare()
            play()
        }
    }

    @AfterTest
    fun tearDown() {
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `is current media item live as flow, no media item`() = runTest {
        player.isCurrentMediaItemLiveAsFlow().test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, vod`() = runTest {
        player.setMediaItem(MediaItem.fromUri(VOD))

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, live`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE))

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, live dvr`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, remove media item`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, transition vod to live dvr`() = runTest {
        player.setMediaItems(listOf(MediaItem.fromUri(VOD), MediaItem.fromUri(LIVE_DVR)))

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, transition live dvr to vod`() = runTest {
        player.setMediaItems(listOf(MediaItem.fromUri(LIVE_DVR), MediaItem.fromUri(VOD)))

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, no media item`() = runTest {
        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(C.TIME_UNSET, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, vod`() = runTest {
        player.setMediaItem(MediaItem.fromUri(VOD))

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, live`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE))

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, live dvr`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.getCurrentDefaultPositionAsFlow().test {
            val duration = player.duration.milliseconds.inWholeHours
            val currentDefaultPositionInHours = awaitItem().milliseconds.inWholeHours

            assertTrue(currentDefaultPositionInHours in duration - 1..duration + 1)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, remove media item`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(C.TIME_UNSET, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, transition vod to live dvr`() = runTest {
        player.setMediaItems(listOf(MediaItem.fromUri(VOD), MediaItem.fromUri(LIVE)))

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.seekToNextMediaItem()

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, transition live dvr to vod`() = runTest {
        player.setMediaItems(listOf(MediaItem.fromUri(LIVE), MediaItem.fromUri(VOD)))

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.seekToNextMediaItem()

        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    private companion object {
        private const val VOD = "https://rts-vod-amd.akamaized.net/ww/14970442/4dcba1d3-8cc8-3667-a7d2-b3b92c4243d9/master.m3u8"
        private const val LIVE = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
        private const val LIVE_DVR = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
    }
}
