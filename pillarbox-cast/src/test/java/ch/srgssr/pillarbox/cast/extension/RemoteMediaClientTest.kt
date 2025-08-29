/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.extension

import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.utils.StringUtil
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.media.MediaQueue
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.asserter

@RunWith(AndroidJUnit4::class)
class RemoteMediaClientTest {

    private lateinit var remoteMediaClient: RemoteMediaClient

    @BeforeTest
    fun setupTests() {
        remoteMediaClient = mockk(relaxed = false)
    }

    @AfterTest
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
    fun `getContentDurationMs returns TIME_UNSET`() {
        every { remoteMediaClient.isLiveStream } returns false
        every { remoteMediaClient.streamDuration } returns MediaInfo.UNKNOWN_DURATION
        assertEquals(C.TIME_UNSET, remoteMediaClient.getContentDurationMs())
    }

    @Test
    fun `getContentDurationMs returns streamDuration`() {
        every { remoteMediaClient.isLiveStream } returns false
        every { remoteMediaClient.streamDuration } returns 12334L
        assertEquals(12334L, remoteMediaClient.getContentDurationMs())
    }

    @Test
    fun `getContentDurationMs for live stream returns live window length`() {
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
            MediaStatus.PLAYER_STATE_UNKNOWN,
            Int.MAX_VALUE, // Invalid state
        )

        assertEquals(Player.STATE_READY, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_READY, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_BUFFERING, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_BUFFERING, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_IDLE, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_IDLE, remoteMediaClient.getPlaybackState())
        assertEquals(Player.STATE_IDLE, remoteMediaClient.getPlaybackState())
    }

    @Test
    fun `getCurrentMediaItemIndex returns INVALID_ITEM_ID when not currentItem`() {
        every { remoteMediaClient.mediaStatus } returns null
        assertEquals(MediaQueueItem.INVALID_ITEM_ID, remoteMediaClient.getCurrentMediaItemIndex())
    }

    @Test
    fun `getCurrentMediaItemIndex returns current index`() {
        val mediaQueue = mockk<MediaQueue>()
        val mediaStatus = mockk<MediaStatus>()
        val currentMediaQueueItem = mockk<MediaQueueItem>()

        every { mediaStatus.currentItemId } returns 1
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        every { mediaQueue.itemCount } returns 10
        every { remoteMediaClient.mediaQueue } returns mediaQueue
        every { remoteMediaClient.currentItem } returns currentMediaQueueItem
        every { currentMediaQueueItem.itemId } returns 1
        every { mediaQueue.indexOfItemWithId(1) } returns 2

        assertEquals(2, remoteMediaClient.getCurrentMediaItemIndex())
    }

    @Test
    fun `getRepeatMode with null mediaStatus`() {
        every { remoteMediaClient.mediaStatus } returns null

        assertEquals(Player.REPEAT_MODE_OFF, remoteMediaClient.getRepeatMode())
    }

    @Test
    fun `getRepeatMode returns Player repeat mode`() {
        val mediaStatus = mockk<MediaStatus>()
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        every { mediaStatus.queueRepeatMode } returnsMany listOf(
            MediaStatus.REPEAT_MODE_REPEAT_OFF,
            MediaStatus.REPEAT_MODE_REPEAT_SINGLE,
            MediaStatus.REPEAT_MODE_REPEAT_ALL,
            MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE,
            Int.MAX_VALUE, // Invalid mode
        )

        assertEquals(Player.REPEAT_MODE_OFF, remoteMediaClient.getRepeatMode())
        assertEquals(Player.REPEAT_MODE_ONE, remoteMediaClient.getRepeatMode())
        assertEquals(Player.REPEAT_MODE_ALL, remoteMediaClient.getRepeatMode())
        assertEquals(Player.REPEAT_MODE_ALL, remoteMediaClient.getRepeatMode())
        assertEquals(Player.REPEAT_MODE_OFF, remoteMediaClient.getRepeatMode())
    }

    @Test
    fun `getVolume returns 1 when mediaStatus is null`() {
        every { remoteMediaClient.mediaStatus } returns null
        assertEquals(1.0, remoteMediaClient.getVolume())
    }

    @Test
    fun `getVolume returns streamVolume`() {
        val mediaStatus = mockk<MediaStatus>()
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        every { mediaStatus.streamVolume } returns 0.5
        assertEquals(0.5, remoteMediaClient.getVolume())
    }

    @Test
    fun `Playback rate is positive`() {
        val mediaStatus = mockk<MediaStatus>()
        every { mediaStatus.playbackRate } returns 0.5
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        assertEquals(0.5f, remoteMediaClient.getPlaybackRate())
    }

    @Test
    fun `Playback rate is negative returns default speed`() {
        val mediaStatus = mockk<MediaStatus>()
        every { mediaStatus.playbackRate } returns -0.5
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        assertEquals(PlaybackParameters.DEFAULT.speed, remoteMediaClient.getPlaybackRate())
    }

    @Test
    fun `Playback rate is zero returns default speed`() {
        val mediaStatus = mockk<MediaStatus>()
        every { mediaStatus.playbackRate } returns 0.0
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        assertEquals(PlaybackParameters.DEFAULT.speed, remoteMediaClient.getPlaybackRate())
    }

    @Test
    fun `Media status is null`() {
        every { remoteMediaClient.mediaStatus } returns null
        assertEquals(PlaybackParameters.DEFAULT.speed, remoteMediaClient.getPlaybackRate())
    }

    @Test
    fun `Playback rate is a small positive number`() {
        val mediaStatus = mockk<MediaStatus>()
        every { mediaStatus.playbackRate } returns Double.MIN_VALUE
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        assertEquals(PlaybackParameters.DEFAULT.speed, remoteMediaClient.getPlaybackRate())
    }

    @Test
    fun `Playback rate is a small negative number`() {
        val mediaStatus = mockk<MediaStatus>()
        every { mediaStatus.playbackRate } returns -Double.MIN_VALUE
        every { remoteMediaClient.mediaStatus } returns mediaStatus
        assertEquals(PlaybackParameters.DEFAULT.speed, remoteMediaClient.getPlaybackRate())
    }

    @Test
    fun `getAvailableCommands repeat mode OFF, current item is first`() {
        every { remoteMediaClient.approximateStreamPosition } returns 200L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 1_000L
        every { remoteMediaClient.approximateLiveSeekableRangeEnd } returns 10_000L
        every { remoteMediaClient.isLiveStream } returns true
        every { remoteMediaClient.mediaStatus } returns mockk {
            every { isPlayingAd } returns false
            every { isMediaCommandSupported(any()) } returns false
            every { currentItemId } returns 0
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns 10
            every { indexOfItemWithId(any()) } returnsArgument 0
        }
        every { remoteMediaClient.playerState } returns MediaStatus.PLAYER_STATE_PLAYING

        val expectedCommands = PERMANENT_AVAILABLE_COMMANDS.buildUpon()
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            .build()

        assertEquals(expectedCommands, remoteMediaClient.getAvailableCommands(5_000L, 10_000L))
    }

    @Test
    fun `getAvailableCommands repeat mode ONE, current item is first`() {
        every { remoteMediaClient.approximateStreamPosition } returns 200L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 1_000L
        every { remoteMediaClient.approximateLiveSeekableRangeEnd } returns 10_000L
        every { remoteMediaClient.isLiveStream } returns true
        every { remoteMediaClient.mediaStatus } returns mockk {
            every { isPlayingAd } returns false
            every { isMediaCommandSupported(any()) } returns false
            every { currentItemId } returns 0
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_SINGLE
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns 10
            every { indexOfItemWithId(any()) } returnsArgument 0
        }
        every { remoteMediaClient.playerState } returns MediaStatus.PLAYER_STATE_PLAYING

        val expectedCommands = PERMANENT_AVAILABLE_COMMANDS.buildUpon()
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            .build()

        assertEquals(expectedCommands, remoteMediaClient.getAvailableCommands(5_000L, 10_000L))
    }

    @Test
    fun `getAvailableCommands repeat mode ALL, current item is first`() {
        every { remoteMediaClient.approximateStreamPosition } returns 200L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 1_000L
        every { remoteMediaClient.approximateLiveSeekableRangeEnd } returns 10_000L
        every { remoteMediaClient.isLiveStream } returns true
        every { remoteMediaClient.mediaStatus } returns mockk {
            every { isPlayingAd } returns false
            every { isMediaCommandSupported(any()) } returns false
            every { currentItemId } returns 0
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_ALL
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns 10
            every { indexOfItemWithId(any()) } returnsArgument 0
        }
        every { remoteMediaClient.playerState } returns MediaStatus.PLAYER_STATE_PLAYING

        val expectedCommands = PERMANENT_AVAILABLE_COMMANDS.buildUpon()
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            .build()

        assertEquals(expectedCommands, remoteMediaClient.getAvailableCommands(5_000L, 10_000L))
    }

    @Test
    fun `getAvailableCommands repeat mode OFF, current item is last`() {
        val itemsCount = 10

        every { remoteMediaClient.approximateStreamPosition } returns 200L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 1_000L
        every { remoteMediaClient.approximateLiveSeekableRangeEnd } returns 10_000L
        every { remoteMediaClient.isLiveStream } returns true
        every { remoteMediaClient.mediaStatus } returns mockk {
            every { isPlayingAd } returns false
            every { isMediaCommandSupported(any()) } returns false
            every { currentItemId } returns itemsCount - 1
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemsCount
            every { indexOfItemWithId(any()) } returnsArgument 0
        }
        every { remoteMediaClient.playerState } returns MediaStatus.PLAYER_STATE_PLAYING

        val expectedCommands = PERMANENT_AVAILABLE_COMMANDS.buildUpon()
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            .build()

        assertEquals(expectedCommands, remoteMediaClient.getAvailableCommands(5_000L, 10_000L))
    }

    @Test
    fun `getAvailableCommands repeat mode ONE, current item is last`() {
        val itemsCount = 10

        every { remoteMediaClient.approximateStreamPosition } returns 200L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 1_000L
        every { remoteMediaClient.approximateLiveSeekableRangeEnd } returns 10_000L
        every { remoteMediaClient.isLiveStream } returns true
        every { remoteMediaClient.mediaStatus } returns mockk {
            every { isPlayingAd } returns false
            every { isMediaCommandSupported(any()) } returns false
            every { currentItemId } returns itemsCount - 1
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_SINGLE
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemsCount
            every { indexOfItemWithId(any()) } returnsArgument 0
        }
        every { remoteMediaClient.playerState } returns MediaStatus.PLAYER_STATE_PLAYING

        val expectedCommands = PERMANENT_AVAILABLE_COMMANDS.buildUpon()
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            .build()

        assertEquals(expectedCommands, remoteMediaClient.getAvailableCommands(5_000L, 10_000L))
    }

    @Test
    fun `getAvailableCommands repeat mode ALL, current item is last`() {
        val itemsCount = 10

        every { remoteMediaClient.approximateStreamPosition } returns 200L
        every { remoteMediaClient.approximateLiveSeekableRangeStart } returns 1_000L
        every { remoteMediaClient.approximateLiveSeekableRangeEnd } returns 10_000L
        every { remoteMediaClient.isLiveStream } returns true
        every { remoteMediaClient.mediaStatus } returns mockk {
            every { isPlayingAd } returns false
            every { isMediaCommandSupported(any()) } returns false
            every { currentItemId } returns itemsCount - 1
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_ALL
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemsCount
            every { indexOfItemWithId(any()) } returnsArgument 0
        }
        every { remoteMediaClient.playerState } returns MediaStatus.PLAYER_STATE_PLAYING

        val expectedCommands = PERMANENT_AVAILABLE_COMMANDS.buildUpon()
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            .build()

        assertEquals(expectedCommands, remoteMediaClient.getAvailableCommands(5_000L, 10_000L))
    }

    @Test
    fun `getPreviousMediaItemIndex, repeat mode OFF`() {
        val itemCountValue = 10

        every { remoteMediaClient.mediaStatus } returns mockk {
            every { currentItemId } returnsMany listOf(0, itemCountValue / 2, itemCountValue - 1)
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemCountValue
            every { indexOfItemWithId(any()) } returnsArgument 0
        }

        listOf(-1, 4, 8).forEach { nextIndex ->
            assertEquals(nextIndex, remoteMediaClient.getPreviousMediaItemIndex())
        }
    }

    @Test
    fun `getPreviousMediaItemIndex, repeat mode ONE`() {
        val itemCountValue = 10

        every { remoteMediaClient.mediaStatus } returns mockk {
            every { currentItemId } returnsMany listOf(0, itemCountValue / 2, itemCountValue - 1)
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_SINGLE
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemCountValue
            every { indexOfItemWithId(any()) } returnsArgument 0
        }

        listOf(0, 5, 9).forEach { nextIndex ->
            assertEquals(nextIndex, remoteMediaClient.getPreviousMediaItemIndex())
        }
    }

    @Test
    fun `getPreviousMediaItemIndex, repeat mode ALL`() {
        val itemCountValue = 10

        every { remoteMediaClient.mediaStatus } returns mockk {
            every { currentItemId } returnsMany listOf(0, itemCountValue / 2, itemCountValue - 1)
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_ALL
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemCountValue
            every { indexOfItemWithId(any()) } returnsArgument 0
        }

        listOf(9, 4, 8).forEach { nextIndex ->
            assertEquals(nextIndex, remoteMediaClient.getPreviousMediaItemIndex())
        }
    }

    @Test
    fun `getNextMediaItemIndex, repeat mode OFF`() {
        val itemCountValue = 10

        every { remoteMediaClient.mediaStatus } returns mockk {
            every { currentItemId } returnsMany listOf(0, itemCountValue / 2, itemCountValue - 1)
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemCountValue
            every { indexOfItemWithId(any()) } returnsArgument 0
        }

        listOf(1, 6, 10).forEach { nextIndex ->
            assertEquals(nextIndex, remoteMediaClient.getNextMediaItemIndex())
        }
    }

    @Test
    fun `getNextMediaItemIndex, repeat mode ONE`() {
        val itemCountValue = 10

        every { remoteMediaClient.mediaStatus } returns mockk {
            every { currentItemId } returnsMany listOf(0, itemCountValue / 2, itemCountValue - 1)
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_SINGLE
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemCountValue
            every { indexOfItemWithId(any()) } returnsArgument 0
        }

        listOf(0, 5, 9).forEach { nextIndex ->
            assertEquals(nextIndex, remoteMediaClient.getNextMediaItemIndex())
        }
    }

    @Test
    fun `getNextMediaItemIndex, repeat mode ALL`() {
        val itemCountValue = 10

        every { remoteMediaClient.mediaStatus } returns mockk {
            every { currentItemId } returnsMany listOf(0, itemCountValue / 2, itemCountValue - 1)
            every { queueRepeatMode } returns MediaStatus.REPEAT_MODE_REPEAT_ALL
        }
        every { remoteMediaClient.mediaQueue } returns mockk {
            every { itemCount } returns itemCountValue
            every { indexOfItemWithId(any()) } returnsArgument 0
        }

        listOf(1, 6, 0).forEach { nextIndex ->
            assertEquals(nextIndex, remoteMediaClient.getNextMediaItemIndex())
        }
    }

    private companion object {
        private fun assertEquals(expected: Player.Commands, actual: Player.Commands, message: String? = null) {
            asserter.assertTrue(
                lazyMessage = {
                    val messagePrefix = if (message == null) "" else "$message. "
                    val expectedCommands = StringUtil.playerCommandsString(expected)
                    val actualCommands = StringUtil.playerCommandsString(actual)

                    "${messagePrefix}Expected <$expectedCommands>, actual <$actualCommands>."
                },
                actual = actual == expected,
            )
        }
    }
}
