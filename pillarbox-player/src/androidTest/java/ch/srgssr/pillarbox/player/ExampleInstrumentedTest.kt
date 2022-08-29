/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.ConditionVariable
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.player.data.MediaItemSource
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun mediaItemFromMediaItemSource() {
        // Context of the app under test.
        val appContext = getInstrumentation().targetContext
        val customTag = "TagForTest"
        val url = "https://media.swissinfo.ch/media/video/dddaff93-c2cd-4b6e-bdad-55f75a519480/rendition/154a844b-de1d-4854-93c1-5c61cd07e98c.mp4"
        val atomicPlayer = AtomicReference<PillarboxPlayer>()
        val waitForReady = WaitReadyListener()
        getInstrumentation().runOnMainSync {
            val player = PillarboxPlayer(
                appContext,
                object : MediaItemSource {
                    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
                        return mediaItem.buildUpon()
                            .setUri(url)
                            .setTag(customTag)
                            .build()
                    }
                }
            )
            atomicPlayer.set(player)
            player.addMediaItem(MediaItem.Builder().setMediaId("DummyId").build())
            player.addListener(waitForReady)
            player.prepare()
            player.play()
        }

        waitForReady.block()

        getInstrumentation().runOnMainSync {
            val player = atomicPlayer.get()
            Assert.assertEquals(Player.STATE_READY, player.playbackState)
            Assert.assertNotNull(player.currentMediaItem)
            Assert.assertEquals(player.currentMediaItem?.localConfiguration?.uri, Uri.parse(url))
            Assert.assertEquals(customTag, player.currentMediaItem?.localConfiguration?.tag)
            player.release()
        }
    }

    private class WaitReadyListener : Player.Listener {
        private val playbackEnded = ConditionVariable()

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                playbackEnded.open()
            }
        }

        fun block() {
            playbackEnded.block()
        }
    }
}
