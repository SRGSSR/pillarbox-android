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
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
            val currentDefaultPositionInHours = awaitItem().milliseconds.inWholeHours

            assertTrue(currentDefaultPositionInHours in 5..6)
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
        private const val VOD = "https://rts-vod-amd.akamaized.net/ww/14970442/7510ee63-05a4-3d48-8d26-1f1b3a82f6be/master.m3u8"
        private const val LIVE = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
        private const val LIVE_DVR = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
    }
}
