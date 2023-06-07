/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.Commands
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.player.test.utils.PlayerListenerCommander
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestPlayerCallbackFlow {
    private lateinit var player: Player
    private lateinit var dispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        dispatcher = UnconfinedTestDispatcher()
        player = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testCurrentPositionWhilePlaying() = runTest {
        val positions = listOf(C.TIME_UNSET, 0L, 1000L, 2000L, 3000L, 4000L, 5000L)
        every { player.currentPosition } returnsMany positions
        every { player.isPlaying } returns true

        val currentPositionFlow = player.currentPositionAsFlow()
        val actualPositions = currentPositionFlow.take(positions.size).toList()
        Assert.assertEquals(positions, actualPositions)
    }

    /**
     * Test current position while not playing
     * We expected a Timeout as the flow doesn't start
     */
    @Test(expected = TimeoutCancellationException::class)
    fun testCurrentPositionWhileNotPlaying() = runTest {
        val positions = listOf(C.TIME_UNSET, 0L, 1000L, 2000L, 3000L, 4000L, 5000L)
        every { player.currentPosition } returnsMany positions
        every { player.isPlaying } returns false

        val currentPositionFlow = player.currentPositionAsFlow()
        val firstPosition = currentPositionFlow.first()
        Assert.assertEquals(positions[0], firstPosition)

        withTimeout(3_000L) {
            val actualPositions = currentPositionFlow.take(positions.size).toList()
            Assert.assertEquals(positions, actualPositions)
        }
    }

    @Test(timeout = 5_000)
    fun testUpdateCurrentPositionAfterSeek() = runTest {
        val positions = listOf(0L, 1000L, 2000L)
        every { player.isPlaying } returns false // TO disable periodic update
        every { player.currentPosition } returnsMany positions
        val fakePlayer = PlayerListenerCommander(player)
        val playbackSpeedFlow = fakePlayer.currentPositionAsFlow()
        val actualPositions = ArrayList<Long>()
        val job = launch(dispatcher) {
            playbackSpeedFlow.take(positions.size).toList(actualPositions)
        }

        fakePlayer.onPositionDiscontinuity(
            mockk(),
            mockk(),
            Player.DISCONTINUITY_REASON_SEEK
        )

        fakePlayer.onPositionDiscontinuity(
            mockk(),
            mockk(),
            Player.DISCONTINUITY_REASON_SEEK
        )

        Assert.assertEquals(positions, actualPositions)
        job.cancel()
    }

    @Test(timeout = 10_000L)
    fun testDuration() = runTest {
        val durations = listOf(1_000L, 5_000L, 10_000L, 20_000L)
        every { player.duration } returnsMany durations

        val fakePlayer = PlayerListenerCommander(player)
        val durationFlow = fakePlayer.durationAsFlow()

        val actualDuration = ArrayList<Long>()
        val job = launch(dispatcher) {
            durationFlow.take(durations.size).toList(actualDuration)
        }
        fakePlayer.onPlaybackStateChanged(Player.STATE_READY)
        fakePlayer.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        fakePlayer.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)
        Assert.assertEquals(durations, actualDuration)
        job.cancel()
    }

    @Test(timeout = 2_000)
    fun testIsPlaying() = runTest {
        every { player.isPlaying } returns false
        val fakePlayer = PlayerListenerCommander(player)
        val isPlayFlow = fakePlayer.isPlayingAsFlow()

        val isPlaying = listOf(false, false, true, false, true)
        val actualIsPlaying = ArrayList<Boolean>()
        val job = launch(dispatcher) {
            isPlayFlow.take(isPlaying.size).toList(actualIsPlaying)
        }
        for (playing in listOf(false, true, false, true)) {
            fakePlayer.onIsPlayingChanged(playing)
        }
        Assert.assertEquals(isPlaying, actualIsPlaying)
        job.cancel()
    }

    @Test(timeout = 5_000)
    fun testPlaybackState() = runTest {
        every { player.playbackState } returns Player.STATE_IDLE
        val fakePlayer = PlayerListenerCommander(player)
        val playbackStateFlow = fakePlayer.playbackStateAsFlow()
        val actualPlaybackStates = ArrayList<Int>()
        val playbackStates =
            listOf(
                Player.STATE_IDLE,
                Player.STATE_BUFFERING,
                Player.STATE_READY,
                Player.STATE_BUFFERING,
                Player.STATE_READY,
                Player.STATE_ENDED
            )
        val job = launch(dispatcher) {
            playbackStateFlow.take(playbackStates.size).toList(actualPlaybackStates)
        }

        fakePlayer.onPlaybackStateChanged(Player.STATE_BUFFERING)
        fakePlayer.onPlaybackStateChanged(Player.STATE_READY)
        fakePlayer.onPlaybackStateChanged(Player.STATE_BUFFERING)
        fakePlayer.onPlaybackStateChanged(Player.STATE_READY)
        fakePlayer.onPlaybackStateChanged(Player.STATE_ENDED)

        Assert.assertEquals(playbackStates, actualPlaybackStates)
        job.cancel()
    }

    @Test(timeout = 2_000)
    fun testError() = runTest {
        val error = mockk<PlaybackException>()
        val noError: PlaybackException? = null
        every { player.playerError } returns null
        val fakePlayer = PlayerListenerCommander(player)
        val errorFlow = fakePlayer.playerErrorAsFlow()
        val actualErrors = ArrayList<PlaybackException?>()
        val errors =
            listOf(
                noError, error, noError
            )
        val job = launch(dispatcher) {
            errorFlow.take(errors.size).toList(actualErrors)
        }

        fakePlayer.onPlayerErrorChanged(error)
        fakePlayer.onPlayerErrorChanged(noError)

        Assert.assertEquals(errors, actualErrors)
        job.cancel()
    }

    @Test(timeout = 2_000)
    fun testAvailableCommands() = runTest {
        val command1 = mockk<Commands>()
        val command2 = mockk<Commands>()
        every { player.availableCommands } returns command1
        val fakePlayer = PlayerListenerCommander(player)
        val commandsFlow = fakePlayer.availableCommandsAsFlow()
        val actualErrors = ArrayList<Commands>()
        val commands = listOf(command1, command2)
        val job = launch(dispatcher) {
            commandsFlow.take(commands.size).toList(actualErrors)
        }

        fakePlayer.onAvailableCommandsChanged(command2)

        Assert.assertEquals(commands, actualErrors)
        job.cancel()
    }

    @Test(timeout = 2_000)
    fun testPlaybackSpeed() = runTest {
        val initialPlaybackRate = 1.5f
        val initialParameters: PlaybackParameters = PlaybackParameters.DEFAULT.withSpeed(initialPlaybackRate)
        every { player.playbackParameters } returns initialParameters
        val fakePlayer = PlayerListenerCommander(player)
        val playbackSpeedFlow = fakePlayer.getPlaybackSpeedAsFlow()
        val actualSpeeds = ArrayList<Float>()
        val speeds = listOf(initialPlaybackRate, 2.0f)
        val job = launch(dispatcher) {
            playbackSpeedFlow.take(speeds.size).toList(actualSpeeds)
        }

        fakePlayer.onPlaybackParametersChanged(initialParameters.withSpeed(2.0f))

        Assert.assertEquals(speeds, actualSpeeds)
        job.cancel()
    }

    @Test(timeout = 5_000)
    fun testCurrentMediaIndex() = runTest {
        val indices = listOf(1, 1, 2)
        every { player.currentMediaItemIndex } returnsMany indices
        val fakePlayer = PlayerListenerCommander(player)
        val currentIndexFlow = fakePlayer.getCurrentMediaItemIndexAsFlow()
        val actualIndices = ArrayList<Int>()
        val job = launch(dispatcher) {
            currentIndexFlow.take(indices.size).toList(actualIndices)
        }

        fakePlayer.onMediaItemTransition(mockk(), Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)

        Assert.assertEquals(indices, actualIndices)
        job.cancel()
    }

    @Test(timeout = 5_000)
    fun testCurrendMediaItem() = runTest {
        val mediaItem1: MediaItem? = null
        val mediaItem2: MediaItem = mockk()
        val mediaItem3: MediaItem = mockk()
        val mediaItems = listOf(mediaItem1, mediaItem2, mediaItem3)
        every { player.currentMediaItem } returnsMany listOf(mediaItem1, mediaItem3)
        val fakePlayer = PlayerListenerCommander(player)
        val currentMediaItemFlow = fakePlayer.currentMediaItemAsFlow()
        val actualMediaItems = ArrayList<MediaItem?>()
        val job = launch(dispatcher) {
            currentMediaItemFlow.take(mediaItems.size).toList(actualMediaItems)
        }

        fakePlayer.onMediaItemTransition(mediaItem2, Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)
        fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)

        Assert.assertEquals(mediaItems, actualMediaItems)
        job.cancel()
    }


    @Test(timeout = 5_000)
    fun testCurrendMediaItems() = runTest {
        val item1 = mockk<MediaItem>()
        val item2 = mockk<MediaItem>()
        val item3 = mockk<MediaItem>()

        val list1 = arrayOf<MediaItem>(item1, item2)
        val list2 = arrayOf<MediaItem>(item1, item2, item3)
        every { player.mediaItemCount } returnsMany listOf(2, 2, 3, 3) // read twice in getCurrentMediaItems!
        every { player.getMediaItemAt(0) } returns item1
        every { player.getMediaItemAt(1) } returns item2
        every { player.getMediaItemAt(2) } returns item3


        val fakePlayer = PlayerListenerCommander(player)

        Assert.assertEquals(item1, fakePlayer.getMediaItemAt(0))
        Assert.assertEquals(item2, fakePlayer.getMediaItemAt(1))
        Assert.assertEquals(item3, fakePlayer.getMediaItemAt(2))

        val currentMediaItemFlow = fakePlayer.getCurrentMediaItemsAsFlow()
        val actualMediaItems = ArrayList<Array<MediaItem>>()
        val job = launch(dispatcher) {
            currentMediaItemFlow.take(2).toList(actualMediaItems)
        }
        fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)

        Assert.assertEquals(2, actualMediaItems.size)
        Assert.assertArrayEquals(list1, actualMediaItems[0])
        Assert.assertArrayEquals(list2, actualMediaItems[1])
        job.cancel()
    }

}
