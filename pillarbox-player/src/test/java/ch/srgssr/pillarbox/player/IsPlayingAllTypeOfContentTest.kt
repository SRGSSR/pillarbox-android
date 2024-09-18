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
import androidx.test.core.app.ApplicationProvider
import ch.srgssr.pillarbox.player.test.utils.TestPillarboxRunHelper
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

        TestPillarboxRunHelper.runUntilIsPlaying(player, isPlaying = true)

        assertEquals(Player.STATE_READY, player.playbackState)
        assertTrue(player.isPlaying)
        assertNotNull(player.currentMediaItem)
        assertEquals(player.currentMediaItem?.localConfiguration?.uri, Uri.parse(urlToTest))
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        @Parameters(name = "{index}: {0}")
        fun parameters(): Iterable<Any> {
            return listOf(
                // From urn:swi:video:48940210
                "https://cdn.prod.swi-services.ch/video-projects/141b30ce-3850-424b-9063-a20d5619d342/localised-videos/ENG/renditions/ENG.mp4",
                "https://rts-vod-amd.akamaized.net/ww/14970442/da2b38fb-ca9f-3c76-80c6-e6fa7f3c2699/master.m3u8",
                "https://srfaudio-a.akamaihd.net/delivery/world/af671f12-6f17-415a-9dd8-b8aee24cce8b.mp3",
                "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
                "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd",
                "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
                "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8",
                "https://stream.srg-ssr.ch/m/la-1ere/mp3_128",
                "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8",
            )
        }
    }
}
