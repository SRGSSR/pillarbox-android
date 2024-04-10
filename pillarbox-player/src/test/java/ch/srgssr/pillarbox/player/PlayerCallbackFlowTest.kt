/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class PlayerCallbackFlowTest {
    private lateinit var clock: FakeClock
    private lateinit var player: ExoPlayer

    @BeforeTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        clock = FakeClock(true)
        player = PillarboxExoPlayer(
            context = context,
            loadControl = DefaultLoadControl(),
            clock = clock,
        ).apply {
            prepare()
            play()
        }
    }

    @AfterTest
    fun tearDown() {
        player.release()
    }

    @Test
    fun `is current media item live as flow, no media item`() = runTest {
        player.isCurrentMediaItemLiveAsFlow().test {
            assertEquals(false, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, vod`() = runTest {
        player.setMediaItem(MediaItem.fromUri(VOD))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertEquals(false, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, live`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertEquals(true, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, live dvr`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertEquals(true, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, remove media item`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertEquals(false, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, transition vod to live dvr`() = runTest {
        player.setMediaItems(listOf(MediaItem.fromUri(VOD), MediaItem.fromUri(LIVE_DVR)))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertEquals(true, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `is current media item live as flow, transition live dvr to vod`() = runTest {
        player.setMediaItems(listOf(MediaItem.fromUri(LIVE_DVR), MediaItem.fromUri(VOD)))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.isCurrentMediaItemLiveAsFlow().test {
            assertEquals(false, awaitItem())
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

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, live`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, live dvr`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(6L, awaitItem().milliseconds.inWholeHours)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, remove media item`() = runTest {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

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

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get current default position as flow, transition live dvr to vod`() = runTest {
        player.setMediaItems(listOf(MediaItem.fromUri(LIVE), MediaItem.fromUri(VOD)))

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.getCurrentDefaultPositionAsFlow().test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    private companion object {
        private const val VOD = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"
        private const val LIVE = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
        private const val LIVE_DVR = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
    }
}
