/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.test.utils.TestPillarboxRunHelper
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestPillarboxPlayerPlaybackSpeed {
    private lateinit var player: PillarboxPlayer

    @Before
    fun createPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        player = PillarboxPlayer(
            context = context,
            clock = FakeClock(true),
            mediaItemSource = object : MediaItemSource {
                override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
                    return mediaItem
                }
            }
        )
    }

    @After
    fun releasePlayer() {
        player.release()
    }

    @Test
    fun `playback speed is always 1x when playing live without dvr`() {
        player.apply {
            setMediaItem(MediaItem.fromUri(LIVE_ONLY_URL))
            prepare()
            play()
        }
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        Assert.assertEquals(Player.STATE_READY, player.playbackState)

        player.setPlaybackSpeed(2f)
        Assert.assertEquals(1f, player.getPlaybackSpeed())

        player.seekTo(0)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.setPlaybackSpeed(2f)
        Assert.assertEquals(1f, player.getPlaybackSpeed())
    }

    @Test
    fun `playback speed is at 1x when at live edge otherwise it can be changed`() {
        player.apply {
            setMediaItem(MediaItem.fromUri(LIVE_DVR_URL))
            prepare()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.setPlaybackSpeed(2f)
        Assert.assertEquals(1f, player.getPlaybackSpeed())

        player.seekTo(0)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.setPlaybackSpeed(2f)
        Assert.assertEquals(2f, player.getPlaybackSpeed())
        TestPillarboxRunHelper.runUntilEvent(player, Player.EVENT_IS_LOADING_CHANGED)
        Assert.assertEquals(2f, player.getPlaybackSpeed())
    }

    @Test
    fun `playback speed goes to 1x when reaching live edge`() {
        player.setMediaItem(MediaItem.fromUri(LIVE_DVR_URL))
        player.prepare()
        player.play()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val liveEdgePosition = player.currentTimeline.getWindow(0, Window()).defaultPositionMs
        Assert.assertNotEquals(C.TIME_UNSET, liveEdgePosition)
        player.seekTo(liveEdgePosition - 5_000)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val speed = 2f
        player.setPlaybackSpeed(speed)
        Assert.assertEquals(speed, player.getPlaybackSpeed())

        TestPillarboxRunHelper.runUntilPlaybackParametersChanged(player)
        Assert.assertEquals(1f, player.getPlaybackSpeed())
    }

    @Test
    fun `playback speed changes when not playing live content`() {
        player.setMediaItem(MediaItem.fromUri(VOD_URL))
        player.prepare()
        player.play()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val speed = 2f
        player.setPlaybackSpeed(speed)
        TestPillarboxRunHelper.runUntilEvent(player)
        Assert.assertEquals(speed, player.getPlaybackSpeed())

        player.setPlaybackSpeed(1f)
        TestPillarboxRunHelper.runUntilEvent(player)
        Assert.assertEquals(1f, player.getPlaybackSpeed())
    }

    companion object {
        const val LIVE_DVR_URL = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
        const val LIVE_ONLY_URL = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
        const val VOD_URL = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"
    }
}
