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
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PlaybackSessionManagerTest {
    private lateinit var clock: FakeClock
    private lateinit var player: ExoPlayer
    private lateinit var sessionManager: PlaybackSessionManager
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
                prepare()
            }

        sessionManager = PlaybackSessionManager().apply {
            setPlayer(player)
            addListener(sessionManagerListener)
        }

        clearMocks(sessionManagerListener)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `get session single media item`() {
        val mediaItem = MediaItem.fromUri(VOD1)

        assertNull(sessionManager.getCurrentSession())
        assertNull(sessionManager.getSessionById("some-invalid-session-id"))

        player.setMediaItem(mediaItem)
        player.play()

        TestPlayerRunHelper.playUntilStartOfMediaItem(player, 0)

        val sessionSlot = slot<PlaybackSessionManager.Session>()

        verify {
            sessionManagerListener.onSessionCreated(capture(sessionSlot))
        }

        val session = sessionManager.getCurrentSession()

        assertNotNull(session)
        assertEquals(sessionSlot.captured.sessionId, session.sessionId)
        assertEquals(session, sessionManager.getSessionById(session.sessionId))
    }

    @Test
    fun `get session multiple media items`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        assertNull(sessionManager.getCurrentSession())
        assertNull(sessionManager.getSessionById("some-invalid-session-id"))

        player.setMediaItems(mediaItems)
        player.play()

        TestPlayerRunHelper.playUntilStartOfMediaItem(player, 2)

        val onSessionCreated = mutableListOf<PlaybackSessionManager.Session>()

        verify {
            sessionManagerListener.onSessionCreated(capture(onSessionCreated))
        }

        val session = sessionManager.getCurrentSession()

        assertEquals(3, onSessionCreated.distinctBy { it.sessionId }.size)
        assertNotNull(session)
        assertEquals(onSessionCreated[1].sessionId, session.sessionId)
        assertEquals(session, sessionManager.getSessionById(session.sessionId))
    }

    @Test
    fun `play single media item`() {
        val mediaItem = MediaItem.fromUri(VOD1)

        player.setMediaItem(mediaItem)
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val onSessionCreated = mutableListOf<PlaybackSessionManager.Session>()
        val onCurrentSession = mutableListOf<PlaybackSessionManager.Session>()

        verify {
            sessionManagerListener.onSessionCreated(capture(onSessionCreated))
            sessionManagerListener.onCurrentSession(capture(onCurrentSession))
        }
        confirmVerified(sessionManagerListener)

        assertEquals(1, onSessionCreated.size)
        assertEquals(1, onCurrentSession.size)

        assertEquals(1, onSessionCreated.distinctBy { it.sessionId }.size)
        assertEquals(1, onCurrentSession.distinctBy { it.sessionId }.size)

        assertEquals(listOf(mediaItem), onSessionCreated.map { it.mediaItem })
        assertEquals(listOf(mediaItem), onCurrentSession.map { it.mediaItem })
    }

    @Test
    fun `play single media item, remove media item`() {
        val mediaItem = MediaItem.fromUri(VOD1)

        player.setMediaItem(mediaItem)
        player.play()
        player.removeMediaItem(player.currentMediaItemIndex)

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

        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val onSessionCreated = mutableListOf<PlaybackSessionManager.Session>()
        val onCurrentSession = mutableListOf<PlaybackSessionManager.Session>()
        val onSessionFinished = mutableListOf<PlaybackSessionManager.Session>()

        verify {
            sessionManagerListener.onSessionCreated(capture(onSessionCreated))
            sessionManagerListener.onCurrentSession(capture(onCurrentSession))
            sessionManagerListener.onSessionFinished(capture(onSessionFinished))
        }
        confirmVerified(sessionManagerListener)

        assertEquals(3, onSessionCreated.size)
        assertEquals(3, onCurrentSession.size)
        assertEquals(3, onSessionFinished.size)

        assertEquals(3, onSessionCreated.distinctBy { it.sessionId }.size)
        assertEquals(3, onCurrentSession.distinctBy { it.sessionId }.size)
        assertEquals(3, onSessionFinished.distinctBy { it.sessionId }.size)

        assertEquals(mediaItems, onSessionCreated.map { it.mediaItem })
        assertEquals(mediaItems, onCurrentSession.map { it.mediaItem })
        assertEquals(mediaItems, onSessionFinished.map { it.mediaItem })
    }

    @Test
    fun `play multiple media items, remove upcoming media item`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }
        val expectedMediaItems = listOf(mediaItems[0], mediaItems[2])

        player.setMediaItems(mediaItems)
        player.play()
        player.removeMediaItem(player.currentMediaItemIndex + 1)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val createdSessions = mutableListOf<PlaybackSessionManager.Session>()
        val currentSessions = mutableListOf<PlaybackSessionManager.Session>()
        val finishedSessions = mutableListOf<PlaybackSessionManager.Session>()

        verify {
            sessionManagerListener.onSessionCreated(capture(createdSessions))
            sessionManagerListener.onCurrentSession(capture(currentSessions))
            sessionManagerListener.onSessionFinished(capture(finishedSessions))
        }
        confirmVerified(sessionManagerListener)

        assertEquals(2, createdSessions.size)
        assertEquals(2, currentSessions.size)
        assertEquals(2, finishedSessions.size)

        assertEquals(2, createdSessions.distinctBy { it.sessionId }.size)
        assertEquals(2, currentSessions.distinctBy { it.sessionId }.size)
        assertEquals(2, finishedSessions.distinctBy { it.sessionId }.size)

        assertEquals(expectedMediaItems, createdSessions.map { it.mediaItem })
        assertEquals(expectedMediaItems, currentSessions.map { it.mediaItem })
        assertEquals(expectedMediaItems, finishedSessions.map { it.mediaItem })
    }

    @Test
    fun `play multiple media items, remove current media item`() {
        val mediaItems = listOf(VOD1, VOD2, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()
        player.removeMediaItem(player.currentMediaItemIndex)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val createdSessions = mutableListOf<PlaybackSessionManager.Session>()
        val currentSessions = mutableListOf<PlaybackSessionManager.Session>()
        val finishedSessions = mutableListOf<PlaybackSessionManager.Session>()

        verify {
            sessionManagerListener.onSessionCreated(capture(createdSessions))
            sessionManagerListener.onCurrentSession(capture(currentSessions))
            sessionManagerListener.onSessionFinished(capture(finishedSessions))
        }
        confirmVerified(sessionManagerListener)

        assertEquals(3, createdSessions.size)
        assertEquals(3, currentSessions.size)
        assertEquals(3, finishedSessions.size)

        assertEquals(3, createdSessions.distinctBy { it.sessionId }.size)
        assertEquals(3, currentSessions.distinctBy { it.sessionId }.size)
        assertEquals(3, finishedSessions.distinctBy { it.sessionId }.size)

        assertEquals(mediaItems, createdSessions.map { it.mediaItem })
        assertEquals(mediaItems, currentSessions.map { it.mediaItem })
        assertEquals(mediaItems, finishedSessions.map { it.mediaItem })
    }

    @Test
    fun `play multiple same media items create multiple sessions`() {
        val mediaItems = listOf(VOD1, VOD1, VOD3).map { MediaItem.fromUri(it) }

        player.setMediaItems(mediaItems)
        player.play()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        // To ensure that the final `onSessionFinished` is triggered.
        player.stop()

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        val createdSessions = mutableListOf<PlaybackSessionManager.Session>()
        val currentSessions = mutableListOf<PlaybackSessionManager.Session>()
        val finishedSessions = mutableListOf<PlaybackSessionManager.Session>()

        verify {
            sessionManagerListener.onSessionCreated(capture(createdSessions))
            sessionManagerListener.onCurrentSession(capture(currentSessions))
            sessionManagerListener.onSessionFinished(capture(finishedSessions))
        }
        confirmVerified(sessionManagerListener)

        assertEquals(3, createdSessions.size)
        assertEquals(3, currentSessions.size)
        assertEquals(3, finishedSessions.size)

        assertEquals(3, createdSessions.distinctBy { it.sessionId }.size)
        assertEquals(3, currentSessions.distinctBy { it.sessionId }.size)
        assertEquals(3, finishedSessions.distinctBy { it.sessionId }.size)

        assertEquals(mediaItems, createdSessions.map { it.mediaItem })
        assertEquals(mediaItems, currentSessions.map { it.mediaItem })
        assertEquals(mediaItems, finishedSessions.map { it.mediaItem })
    }

    private companion object {
        private const val VOD1 = "https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8"
        private const val VOD2 = "https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8"
        private const val VOD3 = "https://rts-vod-amd.akamaized.net/ww/13444466/2787e520-412f-35fb-83d7-8dbb31b5c684/master.m3u8"
    }
}
