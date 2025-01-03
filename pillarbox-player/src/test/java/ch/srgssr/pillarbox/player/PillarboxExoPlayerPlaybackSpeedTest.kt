/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.test.utils.TestPillarboxRunHelper
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(AndroidJUnit4::class)
class PillarboxExoPlayerPlaybackSpeedTest {
    private lateinit var player: PillarboxExoPlayer

    @BeforeTest
    fun createPlayer() {
        player = PillarboxExoPlayer()
    }

    @AfterTest
    fun releasePlayer() {
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `playback speed is always 1x when playing live without dvr`() {
        player.apply {
            setMediaItem(MediaItem.fromUri(LIVE_ONLY_URL))
            prepare()
            play()
        }
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        assertEquals(Player.STATE_READY, player.playbackState)

        player.setPlaybackSpeed(2f)
        assertEquals(1f, player.getPlaybackSpeed())

        player.seekTo(0)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.setPlaybackSpeed(2f)
        assertEquals(1f, player.getPlaybackSpeed())
    }

    @Test
    fun `playback speed is at 1x when at live edge otherwise it can be changed`() {
        player.apply {
            setMediaItem(MediaItem.fromUri(LIVE_DVR_URL))
            prepare()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.setPlaybackSpeed(2f)
        assertEquals(1f, player.getPlaybackSpeed())

        player.seekTo(0)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.setPlaybackSpeed(2f)
        assertEquals(2f, player.getPlaybackSpeed())
        TestPillarboxRunHelper.runUntilEvents(player, Player.EVENT_IS_LOADING_CHANGED)
        assertEquals(2f, player.getPlaybackSpeed())
    }

    @Test
    fun `playback speed goes to 1x when reaching live edge`() {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR_URL))
        player.prepare()
        player.play()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val liveEdgePosition = player.currentTimeline.getWindow(0, Window()).defaultPositionMs
        assertNotEquals(C.TIME_UNSET, liveEdgePosition)
        player.seekTo(liveEdgePosition - 5_000)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val speed = 2f
        player.setPlaybackSpeed(speed)
        assertEquals(speed, player.getPlaybackSpeed())

        TestPillarboxRunHelper.runUntilPlaybackParametersChanged(player)
        assertEquals(1f, player.getPlaybackSpeed())
    }

    @Test
    fun `playback speed changes when not playing live content`() {
        player.setMediaItem(MediaItem.fromUri(VOD_URL))
        player.prepare()
        player.play()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val speed = 2f
        player.setPlaybackSpeed(speed)
        TestPillarboxRunHelper.runUntilEvents(player)
        assertEquals(speed, player.getPlaybackSpeed())

        player.setPlaybackSpeed(1f)
        TestPillarboxRunHelper.runUntilEvents(player)
        assertEquals(1f, player.getPlaybackSpeed())
    }

    private companion object {
        private const val LIVE_DVR_URL = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
        private const val LIVE_ONLY_URL = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
        private const val VOD_URL = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"
    }
}
