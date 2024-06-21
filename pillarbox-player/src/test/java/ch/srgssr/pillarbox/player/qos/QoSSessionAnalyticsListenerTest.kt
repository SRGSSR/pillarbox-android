/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@RunWith(ParameterizedRobolectricTestRunner::class)
class QoSSessionAnalyticsListenerTest(
    private val mediaUrls: List<String>,
) {
    private lateinit var player: Player
    private val qosSessions = mutableListOf<QoSSession>()

    @BeforeTest
    fun setUp() {
        player = createPlayer(mediaUrls) {
            qosSessions.add(it)
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        mediaUrls.forEachIndexed { index, _ ->
            player.seekTo(5.seconds.inWholeMilliseconds)
            player.seekToNextMediaItem()
        }
    }

    @Test
    fun `qos session analytics listener`() {
        assertEquals(mediaUrls, qosSessions.map { it.mediaSource })
    }

    @AfterTest
    fun tearDown() {
        player.release()
        qosSessions.clear()

        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun createPlayer(
        mediaUrls: List<String>,
        callback: (qosSession: QoSSession) -> Unit,
    ): Player {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val listener = QoSSessionAnalyticsListener(context, callback)

        return PillarboxExoPlayer(
            context = context,
            clock = FakeClock(true),
        ).apply {
            val mediaItems = mediaUrls.map(MediaItem::fromUri)

            addMediaItems(mediaItems)
            addAnalyticsListener(listener)
            prepare()
            play()
        }
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{index}: {0}")
        fun parameters(): Iterable<Any> {
            return listOf(
                // AOD only
                listOf("https://rts-aod-dd.akamaized.net/ww/14965091/0ba47e81-57d5-3bc0-b3e5-18c93cc84da3.mp3"),
                // VOD only
                listOf("https://rts-vod-amd.akamaized.net/ww/14981648/85b72399-a98c-3455-bdad-70c82cdf0a30/master.m3u8"),
                // Live DVR audio only
                listOf("https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8"),
                // Live DVR video only
                listOf("https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"),
                // Playlist with mixed content
                listOf(
                    "https://rts-aod-dd.akamaized.net/ww/14967482/effca0a1-d59d-3b64-b7eb-cc58a4ad75d6.mp3",
                    "https://rts-vod-amd.akamaized.net/ww/14983127/c9a205b7-0b47-35ac-989a-9306883470bf/master.m3u8",
                ),
            )
        }
    }
}
