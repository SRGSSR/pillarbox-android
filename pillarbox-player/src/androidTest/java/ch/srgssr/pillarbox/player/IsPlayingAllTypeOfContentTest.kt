/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.ConditionVariable
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.player.utils.ContentUrls
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(Parameterized::class)
class IsPlayingAllTypeOfContentTest {
    @Parameterized.Parameter
    lateinit var urlToTest: String

    @Test
    fun isPlayingTest() {
        // Context of the app under test.
        val appContext = getInstrumentation().targetContext
        val atomicPlayer = AtomicReference<PillarboxExoPlayer>()
        val waitIsPlaying = WaitIsPlaying()
        getInstrumentation().runOnMainSync {
            val player = PillarboxExoPlayer(appContext, Default)
            atomicPlayer.set(player)
            player.addMediaItem(MediaItem.fromUri(urlToTest))
            player.addListener(waitIsPlaying)
            player.prepare()
            player.play()
        }

        waitIsPlaying.block()

        getInstrumentation().runOnMainSync {
            val player = atomicPlayer.get()
            // Make test flaky because dependant of internet
            if (player.playerError != null) {
                throw Exception(player.playerError)
            }
            assertEquals(Player.STATE_READY, player.playbackState)
            assertTrue(player.isPlaying)
            assertNotNull(player.currentMediaItem)
            assertEquals(player.currentMediaItem?.localConfiguration?.uri, Uri.parse(urlToTest))
            player.release()
        }
    }

    private class WaitIsPlaying : Player.Listener {
        private val isPlaying = ConditionVariable()

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                this.isPlaying.open()
            }
        }

        /**
         * Don't block test if a player error occurred
         * @param error
         */
        override fun onPlayerError(error: PlaybackException) {
            isPlaying.open()
        }

        fun block() {
            isPlaying.block()
        }
    }

    companion object {
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun parameters(): Iterable<Any> {
            return listOf(
                ContentUrls.VOD_MP4,
                ContentUrls.VOD_HLS,
                ContentUrls.AOD_MP3,
                ContentUrls.VOD_DASH_H264,
                ContentUrls.VOD_DASH_H265,
                ContentUrls.LIVE_HLS,
                ContentUrls.LIVE_DVR_HLS,
                ContentUrls.AUDIO_LIVE_DVR_HLS,
                ContentUrls.AUDIO_LIVE_MP3
            )
        }
    }
}
