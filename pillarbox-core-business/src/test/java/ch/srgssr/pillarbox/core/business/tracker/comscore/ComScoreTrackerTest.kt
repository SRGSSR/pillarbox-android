/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.comscore

import androidx.media3.common.Player
import androidx.media3.common.util.Size
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import com.comscore.Analytics
import com.comscore.streaming.StreamingAnalytics
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class ComScoreTrackerTest {

    @BeforeTest
    fun setup() {
        mockkStatic(Analytics::class)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initialisation setMediaPlayerName and MediaPlayerVersion`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        ComScoreTracker(streamingAnalytics = streamingAnalytics)
        verify(exactly = 1) {
            streamingAnalytics.setMediaPlayerName("Pillarbox")
            streamingAnalytics.setMediaPlayerVersion(BuildConfig.VERSION_NAME)
        }
        confirmVerified(streamingAnalytics)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `start() require  non null initial data`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        tracker.start(player = player, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `start() require an instance of ComScoreTracker#Data for initial data`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        tracker.start(player = player, initialData = "data")
    }

    @Test
    fun `start() does not call notify play or buffer start when player can't play`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns false
        every { player.surfaceSize } returns Size(100, 200)
        every { player.playbackState } returns Player.STATE_IDLE
        val assets = mapOf("value1" to "key1")
        tracker.start(player = player, initialData = ComScoreTracker.Data(assets = assets))

        verify(exactly = 1) {
            streamingAnalytics.setMetadata(any())
            streamingAnalytics.createPlaybackSession()
        }
        verify(exactly = 0) {
            streamingAnalytics.notifyPlay()
            streamingAnalytics.notifyBufferStart()
        }
    }

    @Test
    fun `start() call notifyBufferStart`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns false
        every { player.surfaceSize } returns Size(130, 200)
        every { player.playbackState } returns Player.STATE_BUFFERING
        tracker.start(player = player, initialData = ComScoreTracker.Data(assets = mapOf("value1" to "key1")))

        verify(exactly = 1) {
            streamingAnalytics.notifyBufferStart()
        }
    }

    @Test
    fun `start() call notifyPlay`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns true
        every { player.surfaceSize } returns Size(300, 200)
        every { player.playbackState } returns Player.STATE_READY
        tracker.start(player = player, initialData = ComScoreTracker.Data(assets = mapOf("value1" to "key1")))

        verify(exactly = 1) {
            streamingAnalytics.notifyPlay()
        }
    }

    @Test
    fun `start() should not call notifyPlay when not connected to a surface`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns true
        every { player.surfaceSize } returns Size.ZERO
        every { player.playbackState } returns Player.STATE_READY
        tracker.start(player = player, initialData = ComScoreTracker.Data(assets = mapOf("value1" to "key1")))

        verify(exactly = 0) {
            streamingAnalytics.notifyPlay()
        }
    }

    @Test
    fun `stop() should always notifyEnd`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns true
        every { player.surfaceSize } returns Size.ZERO
        every { player.playbackState } returns Player.STATE_READY
        tracker.stop(player = player, MediaItemTracker.StopReason.EoF, 500)
        tracker.stop(player = player, MediaItemTracker.StopReason.Stop, 500)

        verify(exactly = 2) {
            streamingAnalytics.notifyEnd()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `update() require an instance of ComScoreTracker#Data`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns true
        every { player.surfaceSize } returns Size.ZERO
        every { player.playbackState } returns Player.STATE_READY
        tracker.update("data")
    }

    @Test
    fun `update() with a different data`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns true
        every { player.surfaceSize } returns Size.ZERO
        every { player.playbackState } returns Player.STATE_READY
        tracker.start(player, initialData = ComScoreTracker.Data(emptyMap()))
        tracker.update(ComScoreTracker.Data(mapOf("key01" to "value01")))

        verify(exactly = 2) {
            streamingAnalytics.setMetadata(any())
        }
    }

    @Test
    fun `update() with a same data`() {
        val streamingAnalytics: StreamingAnalytics = mockk(relaxed = true)
        val tracker = ComScoreTracker(streamingAnalytics = streamingAnalytics)
        val player = mockk<ExoPlayer>(relaxed = true)
        every { player.isPlaying } returns true
        every { player.surfaceSize } returns Size.ZERO
        every { player.playbackState } returns Player.STATE_READY
        tracker.start(player, initialData = ComScoreTracker.Data(emptyMap()))
        tracker.update(ComScoreTracker.Data(emptyMap()))

        verify(exactly = 1) {
            streamingAnalytics.setMetadata(any())
        }
    }
}
