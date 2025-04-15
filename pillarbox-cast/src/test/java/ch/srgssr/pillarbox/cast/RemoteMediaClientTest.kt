/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.cast.extension.getContentDurationMs
import ch.srgssr.pillarbox.cast.extension.getContentPositionMs
import ch.srgssr.pillarbox.cast.extension.getCurrentMediaItemIndex
import ch.srgssr.pillarbox.cast.extension.getPlaybackState
import ch.srgssr.pillarbox.cast.extension.getRepeatMode
import ch.srgssr.pillarbox.cast.extension.getTracks
import ch.srgssr.pillarbox.cast.extension.getVolume
import ch.srgssr.pillarbox.cast.extension.isMuted
import ch.srgssr.pillarbox.cast.extension.toTrackGroup
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.media.MediaQueue
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class RemoteMediaClientTest {

    private lateinit var remoteMediaClient: RemoteMediaClient

    @Before
    fun setupTests() {
        remoteMediaClient = mockk(relaxed = false)
    }

    @After
    fun afterTests() {
        unmockkAll()
    }

    @Test
    fun `getContentPositionMs returns TIME_UNSET`() {
        every { remoteMediaClient.approximateStreamPosition } returns MediaInfo.UNKNOWN_DURATION
        assertEquals(C.TIME_UNSET, remoteMediaClient.getContentPositionMs())
    }

    @Test
    fun `getContentPositionMs returns position`() {
        every { remoteMediaClient.approximateStreamPosition } returns 120L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 0
        assertEquals(120L, remoteMediaClient.getContentPositionMs())
    }

    @Test
    fun `getContentPositionMs returns position with live window start position`() {
        every { remoteMediaClient.approximateStreamPosition } returns 120L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 20L
        assertEquals(100L, remoteMediaClient.getContentPositionMs())
    }

    @Test
    fun `getContentDuration returns TIME_UNSET`() {
        every { remoteMediaClient.isLiveStream } returns false
        every { remoteMediaClient.streamDuration } returns MediaInfo.UNKNOWN_DURATION
        assertEquals(C.TIME_UNSET, remoteMediaClient.getContentDurationMs())
    }

    @Test
    fun `getContentDuration returns streamDuration`() {
        every { remoteMediaClient.isLiveStream } returns false
        every { remoteMediaClient.streamDuration } returns 12334L
        assertEquals(12334L, remoteMediaClient.getContentDurationMs())
    }

    @Test
    fun `getContentDuration for live stream returns live window length`() {
        every { remoteMediaClient.isLiveStream } returns true
        every { remoteMediaClient.streamDuration } returns 100L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 1000L
        every { remoteMediaClient.approximateLiveSeekableRangeEnd } returns 5000L
        assertEquals(5000L - 1000L, remoteMediaClient.getContentDurationMs())
    }

    @Test
    fun `getPlaybackState return STATE_IDLE when there are no items`() {
        val mediaQueue = mockk<MediaQueue>()
        every { mediaQueue.itemCount } returns 0
        every { remoteMediaClient.mediaQueue } returns mediaQueue
        every { remoteMediaClient.playerState } returns MediaStatus.PLAYER_STATE_PLAYING
        assertEquals(Player.STATE_IDLE, remoteMediaClient.getPlaybackState())
    }

    @Test
    fun `getPlaybackState return ExoPlayer state`() {
        val mediaQueue = mockk<MediaQueue>()
        every { mediaQueue.itemCount } returns 10
        every { remoteMediaClient.mediaQueue } returns mediaQueue
        every { remoteMediaClient.playerState } returnsMany listOf(
            MediaStatus.PLAYER_STATE_PLAYING,
            MediaStatus.PLAYER_STATE_PAUSED,
            MediaStatus.PLAYER_STATE_LOADING,
            MediaStatus.PLAYER_STATE_BUFFERING,
            MediaStatus.PLAYER_STATE_IDLE,
            MediaStatus.PLAYER_STATE_UNKNOWN
        )

        assertEquals(Player.STATE_READY, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_READY, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_BUFFERING, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_BUFFERING, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_IDLE, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_IDLE, remoteMediaClient.getPlaybackState())
    }

    @Test
    fun `getCurrentMediaItemIndex returns INVALID_ITEM_ID when not currentItem`() {
        every { remoteMediaClient.currentItem } returns null
        assertEquals(MediaQueueItem.INVALID_ITEM_ID, remoteMediaClient.getCurrentMediaItemIndex())
    }

    @Test
    fun `getCurrentMediaItemIndex returns current index`() {
        val mediaQueue = mockk<MediaQueue>()
        val currentMediaQueueItem = mockk<MediaQueueItem>()
        every { mediaQueue.itemCount } returns 10
        every { remoteMediaClient.mediaQueue } returns mediaQueue
        every { remoteMediaClient.currentItem } returns currentMediaQueueItem
        every { currentMediaQueueItem.itemId } returns 1
        every { mediaQueue.indexOfItemWithId(1) } returns 2
        assertEquals(2, remoteMediaClient.getCurrentMediaItemIndex())
    }

    @Test
    fun `getRepeatMode returns Player repeat mode`() {
        val mediaStatus = mockk<MediaStatus>()
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        every { mediaStatus.queueRepeatMode } returnsMany listOf(
            MediaStatus.REPEAT_MODE_REPEAT_OFF,
            MediaStatus.REPEAT_MODE_REPEAT_SINGLE,
            MediaStatus.REPEAT_MODE_REPEAT_ALL,
            MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE
        )

        assertEquals(Player.REPEAT_MODE_OFF, remoteMediaClient.getRepeatMode())
        assertEquals(Player.REPEAT_MODE_ONE, remoteMediaClient.getRepeatMode())
        assertEquals(Player.REPEAT_MODE_ALL, remoteMediaClient.getRepeatMode())
        assertEquals(Player.REPEAT_MODE_ALL, remoteMediaClient.getRepeatMode())
    }

    @Test
    fun `getVolume returns 0 when mediaStatus is null`() {
        every { remoteMediaClient.mediaStatus } returns null
        assertEquals(0.0, remoteMediaClient.getVolume())
    }

    @Test
    fun `getVolume returns streamVolume`() {
        val mediaStatus = mockk<MediaStatus>()
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        every { mediaStatus.streamVolume } returns 0.5
        assertEquals(0.5, remoteMediaClient.getVolume())
    }

    @Test
    fun `isMuted returns false when mediaStatus is null`() {
        every { remoteMediaClient.mediaStatus } returns null
        assertEquals(false, remoteMediaClient.isMuted())
    }

    @Test
    fun `isMuted returns isMute`() {
        val mediaStatus = mockk<MediaStatus>()
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        every { mediaStatus.isMute } returns true
        assertEquals(true, remoteMediaClient.isMuted())
    }

    @Test
    fun `getTracks returns EMPTY when mediaInfo is null`() {
        every { remoteMediaClient.mediaInfo } returns null
        assertEquals(Tracks.EMPTY, remoteMediaClient.getTracks())
    }

    @Test
    fun `getTracks returns EMPTY when media tracks is empty`() {
        val mediaInfo = mockk<MediaInfo>()
        every { remoteMediaClient.mediaInfo } returns mediaInfo
        every { mediaInfo.mediaTracks } returns emptyList<MediaTrack>()
        assertEquals(Tracks.EMPTY, remoteMediaClient.getTracks())
    }

    @Test
    fun `getTracks returns Tracks`() {
        val listMediaTrack = listOf(
            MediaTrack.Builder(10, MediaTrack.TYPE_TEXT).build(),
            MediaTrack.Builder(20, MediaTrack.TYPE_TEXT).build(),
            MediaTrack.Builder(30, MediaTrack.TYPE_AUDIO).build(),
        )
        val mediaInfo = mockk<MediaInfo>()
        val mediaStatus = mockk<MediaStatus>()
        every { remoteMediaClient.mediaInfo } returns mediaInfo
        every { remoteMediaClient.mediaStatus } returns mediaStatus

        every { mediaInfo.mediaTracks } returns listMediaTrack
        every { mediaStatus.activeTrackIds } returns longArrayOf(10, 30)

        val tabTrackGroup = listOf(
            Tracks.Group(listMediaTrack[0].toTrackGroup(), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(true)),
            Tracks.Group(listMediaTrack[1].toTrackGroup(), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(false)),
            Tracks.Group(listMediaTrack[2].toTrackGroup(), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(true)),
        )
        assertEquals(Tracks(tabTrackGroup), remoteMediaClient.getTracks())
    }
}
