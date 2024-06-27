/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.test.utils.TestPillarboxRunHelper
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class PlaybackSessionManagerTest {
    private lateinit var clock: FakeClock
    private lateinit var player: Player
    private lateinit var sessionManagerListener: PlaybackSessionManager.Listener

    @BeforeTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        clock = FakeClock(true)
        sessionManagerListener = mockk(relaxed = true)
        player = ExoPlayer.Builder(context)
            .setClock(clock)
            .build()
            .apply {
                addAnalyticsListener(PlaybackSessionManager(sessionManagerListener))
                prepare()
            }
    }

    @Test
    fun `play single media item`() {
        val mediaItem = MediaItem.fromUri(VOD1)

        player.setMediaItem(mediaItem)
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val sessions = mutableListOf<PlaybackSessionManager.Session>()

        verifyOrder {
            sessionManagerListener.onSessionCreated(capture(sessions))
            sessionManagerListener.onCurrentSession(capture(sessions))
        }
        confirmVerified(sessionManagerListener)

        assertEquals(2, sessions.size)
        assertEquals(1, sessions.distinctBy { it.sessionId }.size)
        assertTrue(sessions.all { it.mediaItem == mediaItem })
    }

    @Test
    fun `play single media item, remove media item`() {
        val mediaItem = MediaItem.fromUri(VOD1)

        player.setMediaItem(mediaItem)
        player.play()

        TestPillarboxRunHelper.runUntilPosition(player, 5.seconds, clock)

        player.removeMediaItem(0)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val sessions = mutableListOf<PlaybackSessionManager.Session>()

        verifyOrder {
            sessionManagerListener.onSessionCreated(capture(sessions))
            sessionManagerListener.onCurrentSession(capture(sessions))
            sessionManagerListener.onSessionFinished(capture(sessions))
        }
        confirmVerified(sessionManagerListener)

        assertEquals(3, sessions.size)
        assertEquals(1, sessions.distinctBy { it.sessionId }.size)
        assertTrue(sessions.all { it.mediaItem == mediaItem })
    }

    @Test
    fun `play multiple media items`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val sessions = mutableListOf<PlaybackSessionManager.Session>()

        verifyOrder {
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 1
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 1
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 2
            sessionManagerListener.onSessionFinished(capture(sessions)) // Item 1
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 2
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 3
            sessionManagerListener.onSessionFinished(capture(sessions)) // Item 2
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 3
        }
        confirmVerified(sessionManagerListener)

        assertEquals(8, sessions.size)
        assertEquals(3, sessions.distinctBy { it.sessionId }.size)
        assertEquals(
            listOf(mediaItems[0], mediaItems[0], mediaItems[1], mediaItems[0], mediaItems[1], mediaItems[2], mediaItems[1], mediaItems[2]),
            sessions.map { it.mediaItem }.reversed(),
        )
    }

    @Test
    fun `play multiple media items, remove upcoming media item`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()

        TestPillarboxRunHelper.runUntilPosition(player, 5.seconds, clock)

        player.removeMediaItem(1)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val sessions = mutableListOf<PlaybackSessionManager.Session>()

        verifyOrder {
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 1
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 1
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 3
            sessionManagerListener.onSessionFinished(capture(sessions)) // Item 1
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 3
        }
        confirmVerified(sessionManagerListener)

        // https://rts-vod-amd.akamaized.net/ww/13444466/2787e520-412f-35fb-83d7-8dbb31b5c684/master.m3u8 // Item 3
        // https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8 // Item 1
        // https://rts-vod-amd.akamaized.net/ww/13444466/2787e520-412f-35fb-83d7-8dbb31b5c684/master.m3u8 // Item 3
        // https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8 // Item 1
        // https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8 // Item 1
        // assertEquals(emptyList<Uri>(), sessions.map { it.mediaItem.localConfiguration?.uri })

        assertEquals(5, sessions.size)
        assertEquals(2, sessions.distinctBy { it.sessionId }.size)
        assertEquals(
            listOf(mediaItems[0], mediaItems[0], mediaItems[2], mediaItems[0], mediaItems[2]),
            sessions.map { it.mediaItem }.reversed(),
        )
    }

    @Test
    fun `play multiple media items, remove current media item`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()

        TestPillarboxRunHelper.runUntilPosition(player, 5.seconds, clock)

        player.removeMediaItem(0)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val sessions = mutableListOf<PlaybackSessionManager.Session>()

        verifyOrder {
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 1
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 1
            sessionManagerListener.onSessionFinished(capture(sessions)) // Item 1
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 2
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 2
            sessionManagerListener.onSessionCreated(capture(sessions)) // Item 3
            sessionManagerListener.onSessionFinished(capture(sessions)) // Item 2
            sessionManagerListener.onCurrentSession(capture(sessions)) // Item 3
        }
        confirmVerified(sessionManagerListener)

        assertEquals(8, sessions.size)
        assertEquals(3, sessions.distinctBy { it.sessionId }.size)
        assertEquals(
            listOf(mediaItems[0], mediaItems[0], mediaItems[0], mediaItems[1], mediaItems[1], mediaItems[2], mediaItems[1], mediaItems[2]),
            sessions.map { it.mediaItem }.reversed(),
        )
    }

    @AfterTest
    fun tearDown() {
        player.release()

        shadowOf(Looper.getMainLooper()).idle()
    }

    private companion object {
        private const val VOD1 = "https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8"
        private const val VOD2 = "https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8"
        private const val VOD3 = "https://rts-vod-amd.akamaized.net/ww/13444466/2787e520-412f-35fb-83d7-8dbb31b5c684/master.m3u8"
    }
}
