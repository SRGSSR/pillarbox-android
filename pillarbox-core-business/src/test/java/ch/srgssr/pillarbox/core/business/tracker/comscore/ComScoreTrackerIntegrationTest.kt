/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.comscore

import android.content.Context
import android.os.Looper
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.core.business.PillarboxExoplayer
import ch.srgssr.pillarbox.core.business.SRGMediaItemBuilder
import ch.srgssr.pillarbox.core.business.utils.LocalMediaCompositionWithFallbackService
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import com.comscore.streaming.AssetMetadata
import com.comscore.streaming.StreamingAnalytics
import io.mockk.Called
import io.mockk.MockKVerificationScope
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class ComScoreTrackerIntegrationTest {
    private lateinit var clock: FakeClock
    private lateinit var streamingAnalytics: StreamingAnalytics
    private lateinit var player: Player

    @BeforeTest
    fun setup() {
        clock = FakeClock(true)
        streamingAnalytics = mockk(relaxed = true)

        val comScoreFactory = MediaItemTracker.Factory {
            ComScoreTracker(streamingAnalytics)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mediaCompositionWithFallbackService = LocalMediaCompositionWithFallbackService(context)
        player = PillarboxExoplayer(context) {
            clock(clock)
            coroutineContext(EmptyCoroutineContext)
            srgAssetLoader(context) {
                mediaCompositionService(mediaCompositionWithFallbackService)
                comscoreTrackerFactory(comScoreFactory)
                commanderActTrackerFactory(mockk(relaxed = true))
            }
        }
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `player unprepared`() {
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verify { streamingAnalytics wasNot Called }
    }

    @Test
    fun `player prepared and playing, changing media item`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyLiveInformation()
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifyPlayEvent()
            verifyEndEvent()
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `URL don't send any analytics`() {
        player.setMediaItem(MediaItem.fromUri(URL))
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verify { streamingAnalytics wasNot Called }
    }

    @Test
    @Ignore("SurfaceView/SurfaceHolder not implemented in Robolectric")
    fun `surface size changed`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        // Attach the Player to a surface
        val surfaceView = SurfaceView(ApplicationProvider.getApplicationContext())
        surfaceView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        player.setVideoSurfaceView(surfaceView)

        // The surface has a non-zero size
        surfaceView.updateLayoutParams {
            width = 1280
            height = 720
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertEquals(1280, surfaceView.width)

        // The surface now has a size of 0
        surfaceView.updateLayoutParams {
            width = 0
            height = 0
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // The surface has a non-zero size again
        surfaceView.updateLayoutParams {
            width = 1920
            height = 1080
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Verify that the proper events are sent
        verifyLiveInformation()
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(1f)
            verifyBufferEvents()
            verifyPlayEvent()
            verifyPauseEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    // region Live media
    @Test
    fun `live - player prepared but not playing`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `live - player prepared and playing`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyLiveInformation()
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `live - player prepared, playing and paused`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.pause()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyLiveInformation()
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifyPlayEvent()
            verifyPauseEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `live - player prepared, playing, paused, playing again`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.pause()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(4.minutes.inWholeMilliseconds)
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyLiveInformation()
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifyPlayEvent()
            verifyPauseEvent()
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `live - player prepared, playing and stopped`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.stop()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verifyLiveInformation()
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifyPlayEvent()
            verifyEndEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `live - player prepared, playing and seeking`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.seekTo(30.seconds.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyLiveInformation()
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifyPlayEvent()
            verifySeekStart()
            verifyBufferEvents()
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `live - player prepared and seek`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.seekTo(3.minutes.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyLiveInformation(atLeast = 0)
        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `live - player prepared and stopped`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_LIVE_DVR_VIDEO).build())
        player.prepare()
        player.stop()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verify { streamingAnalytics wasNot Called }
    }
    // endregion

    // region Not live media
    @Test
    fun `not live - player prepared but not playing`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared and playing`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared and playing, change playback speed`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true
        player.setPlaybackSpeed(2f)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 2f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared and playing, change playback speed while playing`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(5.minutes.inWholeMilliseconds)
        player.setPlaybackSpeed(2f)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
            verifyPlaybackRate(playbackRate = 2f)
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared, playing and paused`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.pause()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
            verifyPauseEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared, playing, paused, playing again`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.pause()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(4.minutes.inWholeMilliseconds)
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
            verifyPauseEvent()
            verifySeekEvent(0L)
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared, playing and stopped`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.stop()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
            verifyEndEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `player prepared, playing and released`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        clock.advanceTime(2.minutes.inWholeMilliseconds)
        player.release()

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
            verifyEndEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared, playing and seeking`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.playWhenReady = true

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.seekTo(30.seconds.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
            verifySeekEvent(0L)
            verifyPlayEvent()
            verifySeekStart()
            verifySeekEvent(30_000L)
            verifyBufferEvents()
            verifySeekEvent(30_000L)
            verifyPlayEvent()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared and seek`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.seekTo(3.minutes.inWholeMilliseconds)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            verifyPlayerInformation()
            verifyCreatePlaybackSession()
            verifyMetadata()
            verifyPlaybackRate(playbackRate = 1f)
            verifyBufferEvents()
        }
        confirmVerified(streamingAnalytics)
    }

    @Test
    fun `not live - player prepared and stopped`() {
        player.setMediaItem(SRGMediaItemBuilder(URN_NOT_LIVE_VIDEO).build())
        player.prepare()
        player.stop()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_IDLE)

        verify { streamingAnalytics wasNot Called }
    }
    // endregion

    // region Events verification
    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifyPlayerInformation(
        mediaPlayerName: String = "Pillarbox",
        mediaPlayerVersion: String = BuildConfig.VERSION_NAME,
    ) {
        streamingAnalytics.setMediaPlayerName(mediaPlayerName)
        streamingAnalytics.setMediaPlayerVersion(mediaPlayerVersion)
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifyCreatePlaybackSession() {
        streamingAnalytics.createPlaybackSession()
    }

    private fun MockKVerificationScope.verifyMetadata(metadata: AssetMetadata = any()) {
        streamingAnalytics.setMetadata(metadata)
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifyPlaybackRate(playbackRate: Float) {
        streamingAnalytics.notifyChangePlaybackRate(playbackRate)
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifyPauseEvent() {
        streamingAnalytics.notifyPause()
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifyPlayEvent() {
        streamingAnalytics.notifyPlay()
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifyEndEvent() {
        streamingAnalytics.notifyEnd()
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifyBufferEvents() {
        streamingAnalytics.notifyBufferStart()
        streamingAnalytics.notifyBufferStop()
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifySeekEvent(position: Long) {
        streamingAnalytics.startFromPosition(position)
    }

    @Suppress("UnusedReceiverParameter")
    private fun MockKVerificationScope.verifySeekStart() {
        streamingAnalytics.notifySeekStart()
    }

    private fun verifyLiveInformation(atLeast: Int = 1) {
        verify(atLeast = atLeast) {
            streamingAnalytics.setDvrWindowLength(any())
            streamingAnalytics.startFromDvrWindowOffset(any())
        }
    }
    // endregion

    private companion object {
        private const val URL = "https://rts-vod-amd.akamaized.net/ww/14970442/7510ee63-05a4-3d48-8d26-1f1b3a82f6be/master.m3u8"
        private const val URN_LIVE_DVR_VIDEO = LocalMediaCompositionWithFallbackService.URN_LIVE_DVR_VIDEO
        private const val URN_NOT_LIVE_VIDEO = "urn:rsi:video:15916771"
    }
}
