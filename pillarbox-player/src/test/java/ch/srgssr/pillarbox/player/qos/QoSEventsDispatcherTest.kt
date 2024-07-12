/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class QoSEventsDispatcherTest {
    private lateinit var clock: FakeClock
    private lateinit var player: ExoPlayer
    private lateinit var eventsDispatcherListener: QoSEventsDispatcher.Listener

    @BeforeTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        clock = FakeClock(true)
        eventsDispatcherListener = mockk(relaxed = true)
        player = ExoPlayer.Builder(context)
            .setClock(clock)
            .build()
            .apply {
                prepare()
            }

        val sessionManager = PlaybackSessionManager().apply {
            registerPlayer(player)
        }

        PillarboxEventsDispatcher(sessionManager).apply {
            registerPlayer(player)
            addListener(eventsDispatcherListener)
        }

        clearMocks(eventsDispatcherListener)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `play single media item`() {
        val mediaItem = MediaItem.fromUri(VOD1)

        player.setMediaItem(mediaItem)
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val onIsPlayingSessions = mutableListOf<PlaybackSessionManager.Session>()
        val onIsPlayingValue = mutableListOf<Boolean>()

        verify {
            eventsDispatcherListener.onIsPlaying(capture(onIsPlayingSessions), capture(onIsPlayingValue))
        }
        confirmVerified(eventsDispatcherListener)

        assertEquals(2, onIsPlayingValue.size)
        assertEquals(1, onIsPlayingSessions.distinctBy { it.sessionId }.size)
        assertEquals(listOf(true, false), onIsPlayingValue)
    }

    @Test
    fun `play multiple media items`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // To ensure that the final `onSessionFinished` is triggered.
        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val onIsPlayingSessions = mutableListOf<PlaybackSessionManager.Session>()
        val onIsPlayingValue = mutableListOf<Boolean>()

        verify {
            eventsDispatcherListener.onIsPlaying(capture(onIsPlayingSessions), capture(onIsPlayingValue))
        }
        confirmVerified(eventsDispatcherListener)

        assertEquals(6, onIsPlayingValue.size)
        assertEquals(3, onIsPlayingSessions.distinctBy { it.sessionId }.size)
        assertEquals(listOf(true, false, true, false, true, false), onIsPlayingValue)
    }

    @Test
    fun `play multiple media items, remove upcoming media item`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()
        player.removeMediaItem(player.currentMediaItemIndex + 1)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // To ensure that the final `onSessionFinished` is triggered.
        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val onIsPlayingSessions = mutableListOf<PlaybackSessionManager.Session>()
        val onIsPlayingValue = mutableListOf<Boolean>()

        verify {
            eventsDispatcherListener.onIsPlaying(capture(onIsPlayingSessions), capture(onIsPlayingValue))
        }
        confirmVerified(eventsDispatcherListener)

        assertEquals(4, onIsPlayingValue.size)
        assertEquals(2, onIsPlayingSessions.distinctBy { it.sessionId }.size)
        assertEquals(listOf(true, false, true, false), onIsPlayingValue)
    }

    @Test
    fun `play multiple media items, remove current media item`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()
        player.removeMediaItem(player.currentMediaItemIndex)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // To ensure that the final `onSessionFinished` is triggered.
        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val onIsPlayingSessions = mutableListOf<PlaybackSessionManager.Session>()
        val onIsPlayingValue = mutableListOf<Boolean>()

        verify {
            eventsDispatcherListener.onIsPlaying(capture(onIsPlayingSessions), capture(onIsPlayingValue))
        }
        confirmVerified(eventsDispatcherListener)

        assertEquals(4, onIsPlayingValue.size)
        assertEquals(2, onIsPlayingSessions.distinctBy { it.sessionId }.size)
        assertEquals(listOf(true, false, true, false), onIsPlayingValue)
    }

    @Test
    fun `play multiple same media items create multiple sessions`() {
        val mediaItems = listOf(VOD1, VOD1, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // To ensure that the final `onSessionFinished` is triggered.
        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val onIsPlayingSessions = mutableListOf<PlaybackSessionManager.Session>()
        val onIsPlayingValue = mutableListOf<Boolean>()

        verify {
            eventsDispatcherListener.onIsPlaying(capture(onIsPlayingSessions), capture(onIsPlayingValue))
        }
        confirmVerified(eventsDispatcherListener)

        assertEquals(6, onIsPlayingValue.size)
        assertEquals(3, onIsPlayingSessions.distinctBy { it.sessionId }.size)
        assertEquals(listOf(true, false, true, false, true, false), onIsPlayingValue)
    }

    private companion object {
        private const val VOD1 = "https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8"
        private const val VOD2 = "https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8"
        private const val VOD3 = "https://rts-vod-amd.akamaized.net/ww/13444466/2787e520-412f-35fb-83d7-8dbb31b5c684/master.m3u8"
    }
}
