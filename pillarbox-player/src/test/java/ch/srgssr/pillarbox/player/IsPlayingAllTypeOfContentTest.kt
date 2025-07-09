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
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(ParameterizedRobolectricTestRunner::class)
class IsPlayingAllTypeOfContentTest(
    @Suppress("unused") private val urlLabel: String, // Not used in the test itself, but for the test label
    private val urlToTest: String,
) {
    private lateinit var player: PillarboxExoPlayer

    @BeforeTest
    fun setUp() {
        player = PillarboxExoPlayer(
            context = ApplicationProvider.getApplicationContext(),
            type = Default,
        ) {
            clock(FakeClock(true))
            coroutineContext(EmptyCoroutineContext)
        }
    }

    @AfterTest
    fun tearDown() {
        player.release()

        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `is playing`() {
        player.setMediaItem(MediaItem.fromUri(urlToTest))
        player.prepare()
        player.play()

        TestPlayerRunHelper.play(player).untilBackgroundThreadCondition { player.isPlaying }

        assertEquals(Player.STATE_READY, player.playbackState)
        assertTrue(player.isPlaying)
        assertNotNull(player.currentMediaItem)
        assertEquals(player.currentMediaItem?.localConfiguration?.uri, Uri.parse(urlToTest))
    }

    companion object {
        @JvmStatic
        @Suppress("unused", "MaximumLineLength", "MaxLineLength")
        @Parameters(name = "{0}: {1}")
        fun parameters(): Iterable<Any> {
            return listOf(
                arrayOf(
                    "VOD MP4",
                    "https://cdn.prod.swi-services.ch/video-projects/141b30ce-3850-424b-9063-a20d5619d342/localised-videos/ENG/renditions/ENG.mp4"
                ), // From urn:swi:video:48940210
                arrayOf("VOD HLS", "https://rts-vod-amd.akamaized.net/ww/14970442/4dcba1d3-8cc8-3667-a7d2-b3b92c4243d9/master.m3u8"),
                arrayOf(
                    "AOD MP3",
                    "https://download-media.srf.ch/world/audio/Echo_der_Zeit_radio/2025/01/Echo_der_Zeit_radio_AUDI20250119_RS_0069_8a020b8274994bfdbc724cb0c6ed520c.mp3"
                ),
                arrayOf("VOD DASH H264", "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"),
                arrayOf("VOD DASH H265", "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"),
                arrayOf("Live HLS", "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"),
                arrayOf("Live DVR HLS", "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"),
                arrayOf("Audio Live DVR HLS", "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8"),
                // TODO Investigate why this content does not work
                // arrayOf("Audio Live MP3", "https://stream.srg-ssr.ch/m/la-1ere/mp3_128"),
            )
        }
    }
}
