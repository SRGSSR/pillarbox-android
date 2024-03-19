/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.net.Uri
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(ParameterizedRobolectricTestRunner::class)
class IsPlayingAllTypeOfContentTest(
    private val urlToTest: String
) {
    private lateinit var player: PillarboxExoPlayer

    @BeforeTest
    fun setUp() {
        player = PillarboxExoPlayer(
            context = ApplicationProvider.getApplicationContext(),
            type = Default,
        ) {
            clock(FakeClock(true))
        }
    }

    @AfterTest
    fun tearDown() {
        player.release()

        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `is playing`() {
        player.addMediaItem(MediaItem.fromUri(urlToTest))
        player.prepare()
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        // Make test flaky because dependant of internet
        if (player.playerError != null) {
            throw IllegalStateException(player.playerError)
        }

        assertEquals(Player.STATE_READY, player.playbackState)
        assertTrue(player.isPlaying)
        assertNotNull(player.currentMediaItem)
        assertEquals(player.currentMediaItem?.localConfiguration?.uri, Uri.parse(urlToTest))
    }

    companion object {
        // From urn:swi:video:48940210
        private const val VOD_MP4 =
            "https://cdn.prod.swi-services.ch/video-projects/141b30ce-3850-424b-9063-a20d5619d342/localised-videos/ENG/renditions/ENG.mp4"
        private const val VOD_HLS = "https://rts-vod-amd.akamaized.net/ww/14970442/da2b38fb-ca9f-3c76-80c6-e6fa7f3c2699/master.m3u8"
        private const val AOD_MP3 = "https://srfaudio-a.akamaihd.net/delivery/world/af671f12-6f17-415a-9dd8-b8aee24cce8b.mp3"
        private const val VOD_DASH_H264 = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
        private const val VOD_DASH_H265 = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"
        private const val LIVE_HLS = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
        private const val LIVE_DVR_HLS = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
        private const val AUDIO_LIVE_MP3 = "https://stream.srg-ssr.ch/m/la-1ere/mp3_128"
        private const val AUDIO_LIVE_DVR_HLS = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8"

        @JvmStatic
        @Suppress("unused")
        @Parameters(name = "{index}: {0}")
        fun parameters(): Iterable<Any> {
            return listOf(
                VOD_MP4,
                VOD_HLS,
                AOD_MP3,
                VOD_DASH_H264,
                VOD_DASH_H265,
                LIVE_HLS,
                LIVE_DVR_HLS,
                AUDIO_LIVE_MP3,
                AUDIO_LIVE_DVR_HLS,
            )
        }
    }
}
