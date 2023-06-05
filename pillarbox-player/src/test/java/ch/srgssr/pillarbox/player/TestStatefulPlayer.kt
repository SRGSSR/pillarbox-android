/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.player.test.utils.PlayerListenerCommander
import io.mockk.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestStatefulPlayer {

    private lateinit var player: Player
    private lateinit var scope: CoroutineScope
    private lateinit var dispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        dispatcher = UnconfinedTestDispatcher()
        scope = CoroutineScope(dispatcher)
        player = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        scope.cancel()
        clearAllMocks()
    }

    @Test
    fun testInitialValues() = runTest {
        val availableCommands = mockk<Player.Commands>()
        val currentPosition = 4500L
        val duration = 45_000L
        val error = mockk<PlaybackException>()
        val state = Player.STATE_READY
        val isPlaying = true
        val isLoading = true
        every { player.isPlaying } returns isPlaying
        every { player.isLoading } returns isLoading
        every { player.currentPosition } returns currentPosition
        every { player.duration } returns duration
        every { player.playbackState } returns state
        every { player.playerError } returns error
        every { player.availableCommands } returns availableCommands

        val playerStateful = StatefulPlayer(player, scope)

        val isPlayingValue = playerStateful.isPlayingState.take(1).first()
        val playbackStateValue = playerStateful.playbackStateAsState.take(1).first()
        val currentPositionValue = playerStateful.currentPositionState.take(1).first()
        val durationValue = playerStateful.durationState.take(1).first()
        val currentErrorValue = playerStateful.playerErrorState.take(1).first()
        val availableCommandsValue = playerStateful.availableCommandsState.take(1).first()

        Assert.assertEquals(isPlaying, isPlayingValue)
        Assert.assertEquals(state, playbackStateValue)
        Assert.assertEquals(currentPosition, currentPositionValue)
        Assert.assertEquals(duration, durationValue)
        Assert.assertEquals(error, currentErrorValue)
        Assert.assertEquals(availableCommands, availableCommandsValue)
    }

    @Test(timeout = 2_000)
    fun isPlaying() = runTest {
        every { player.isPlaying } returns false
        val playerListenerCommander = PlayerListenerCommander(player)
        val playerStateful = StatefulPlayer(playerListenerCommander, scope)
        val isPlayFlow = playerStateful.isPlayingState

        Assert.assertFalse(isPlayFlow.value)

        val job = launch(dispatcher) {
            isPlayFlow.collect()
        }
        for (isPlaying in listOf(false, true, false)) {
            playerListenerCommander.onIsPlayingChanged(isPlaying)
            Assert.assertEquals(isPlaying, isPlayFlow.value)
        }
        job.cancel()
    }

    @Test(timeout = 5_000)
    fun playbackState() = runTest {
        every { player.playbackState } returns Player.STATE_IDLE
        val playerListenerCommander = PlayerListenerCommander(player)
        val playerStateful = StatefulPlayer(playerListenerCommander, scope)
        val playbackStateFlow = playerStateful.playbackStateAsState

        Assert.assertEquals(Player.STATE_IDLE, playbackStateFlow.value)

        val job = launch(dispatcher) {
            playbackStateFlow.collect()
        }

        val playbackStates = listOf(Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY, Player.STATE_BUFFERING, Player.STATE_ENDED)
        for (state in playbackStates) {
            playerListenerCommander.onPlaybackStateChanged(state)
            Assert.assertEquals(state, playbackStateFlow.value)
        }
        job.cancel()
    }

    @Test(timeout = 10_000L)
    fun duration() = runTest {
        val durations = listOf(5_000L, 5_000L, 10_000L, 20_000L, 30_000L)
        every { player.duration } returnsMany durations
        val playerListenerCommander = PlayerListenerCommander(player)
        val playerStateful = StatefulPlayer(playerListenerCommander, scope)
        val durationFlow = playerStateful.durationState

        Assert.assertEquals(durations[0], durationFlow.value)

        val job = launch(dispatcher) {
            durationFlow.collect()
        }
        Assert.assertEquals(durations[1], durationFlow.value)
        playerListenerCommander.onPlaybackStateChanged(Player.STATE_READY)
        Assert.assertEquals(durations[2], durationFlow.value)

        playerListenerCommander.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        Assert.assertEquals(durations[3], durationFlow.value)

        playerListenerCommander.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)
        Assert.assertEquals(durations[4], durationFlow.value)

        job.cancel()
    }

    @Test(timeout = 2_000)
    fun playerError() = runTest {
        val errors = listOf(null, mockk<PlaybackException>(), null)
        every { player.playerError } returnsMany errors
        val playerListenerCommander = PlayerListenerCommander(player)
        val playerStateful = StatefulPlayer(playerListenerCommander, scope)
        Assert.assertNull(playerStateful.playerErrorState.value)
        val job = launch(dispatcher) {
            playerStateful.playerErrorState.collect()
        }

        val exception: PlaybackException = errors[1]!!
        playerListenerCommander.onPlayerError(exception)
        playerListenerCommander.onPlayerErrorChanged(exception)
        Assert.assertEquals(exception, playerStateful.playerErrorState.value)

        playerListenerCommander.onPlayerErrorChanged(null)
        Assert.assertNull(playerStateful.playerErrorState.value)


        job.cancel()
    }

    @Test(timeout = 2_000)
    fun availableCommand() = runTest {
        val command1 = mockk<Player.Commands>()
        val command2 = mockk<Player.Commands>()
        val availableCommands = listOf(command1, command2)
        every { player.availableCommands } returnsMany availableCommands
        val playerListenerCommander = PlayerListenerCommander(player)
        val playerStateful = StatefulPlayer(playerListenerCommander, scope)
        val commandsFlow = playerStateful.availableCommandsState
        Assert.assertEquals(command1, commandsFlow.value)
        val job = launch(dispatcher) {
            commandsFlow.collect()
        }

        for (command in availableCommands) {
            playerListenerCommander.onAvailableCommandsChanged(command)
            Assert.assertEquals(command, commandsFlow.value)
        }
        job.cancel()
    }

    @Test(timeout = 5_000)
    fun updateCurrentPositionAfterSeek() = runTest {
        val positions = listOf(C.TIME_UNSET, 0L, 1000L, 2000L)
        every { player.currentPosition } returnsMany positions
        every { player.isPlaying } returns false
        val playerListenerCommander = PlayerListenerCommander(player)
        val playerStateful = StatefulPlayer(playerListenerCommander, scope)
        val currentPositionFlow = playerStateful.currentPositionState
        Assert.assertEquals(positions[0], currentPositionFlow.value)
        val job = launch(dispatcher) {
            currentPositionFlow.collect()
        }
        Assert.assertEquals(positions[1], currentPositionFlow.value)
        playerListenerCommander.onPositionDiscontinuity(
            mockk(),
            mockk(),
            Player.DISCONTINUITY_REASON_SEEK
        )

        Assert.assertEquals(positions[2], currentPositionFlow.value)
        job.cancel()
    }

    @Test(timeout = 2_000)
    fun testPlaybackSpeedChanges() = runTest {
        val initialPlaybackRate = 1.5f
        val initialParameters: PlaybackParameters = PlaybackParameters.DEFAULT.withSpeed(initialPlaybackRate)
        every { player.playbackParameters } returns initialParameters
        val playerListenerCommander = PlayerListenerCommander(player)
        val playerStateful = StatefulPlayer(playerListenerCommander, scope)
        val playbackSpeedFlow = playerStateful.playbackSpeedState
        Assert.assertEquals(initialPlaybackRate, playbackSpeedFlow.value)
        val job = launch(dispatcher) {
            playbackSpeedFlow.collect()
        }

        val newSpeed = 1.0f
        val parameters: PlaybackParameters = PlaybackParameters.DEFAULT.withSpeed(newSpeed)
        playerListenerCommander.onPlaybackParametersChanged(parameters)
        Assert.assertEquals(newSpeed, playbackSpeedFlow.value)

        job.cancel()
    }
}
