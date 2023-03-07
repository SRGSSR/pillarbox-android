/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.viewmodel

import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.player.PlayerListenerCommander
import ch.srgssr.pillarbox.player.PlayerState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestPlayerState {

    private lateinit var player: Player

    @Before
    fun setUp() {
        player = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
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

        val viewModel = PlayerState(player)

        val isPlayingValue = viewModel.isPlaying.take(1).first()
        val isLoadingValue = viewModel.isLoading.take(1).first()
        val playbackStateValue = viewModel.playbackState.take(1).first()
        val currentPositionValue = viewModel.currentPosition.take(1).first()
        val durationValue = viewModel.duration.take(1).first()
        val currentErrorValue = viewModel.playerError.take(1).first()
        val availableCommandsValue = viewModel.availableCommands.take(1).first()

        Assert.assertEquals(isPlaying, isPlayingValue)
        Assert.assertEquals(isLoading, isLoadingValue)
        Assert.assertEquals(state, playbackStateValue)
        Assert.assertEquals(currentPosition, currentPositionValue)
        Assert.assertEquals(duration, durationValue)
        Assert.assertEquals(error, currentErrorValue)
        Assert.assertEquals(availableCommands, availableCommandsValue)
    }

    @Test
    fun periodicTickerWhilePlaying() = runTest {
        every { player.isPlaying } returns true
        every { player.currentPosition } returnsMany listOf(12_000L, 13_000L, 14_000L)
        val viewModel = PlayerState(player)
        val currentPositionValues = viewModel.currentPosition.take(3).toList()

        Assert.assertEquals(listOf(12_000L, 13_000L, 14_000L), currentPositionValues)
    }

    /**
     * We expect a Timeout as periodic update won't start.
     */
    @Test(expected = TimeoutCancellationException::class)
    fun periodicTickerWhenNotPlaying() = runTest {
        every { player.isPlaying } returns false
        every { player.currentPosition } returnsMany listOf(12_000L, 13_000L, 14_000L)
        val viewModel = PlayerState(player)
        withTimeout(2_000) {
            viewModel.currentPosition.take(2).toList() // emit only once because ticker not started.
        }
    }

    @Test(timeout = 2_000)
    fun isPlaying() = runTest {
        every { player.isPlaying } returns false
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)
        launch {
            delay(100)
            playerListenerCommander.onIsPlayingChanged(true)
            delay(100)
            playerListenerCommander.onIsPlayingChanged(false)
        }

        val isPlayingValues = viewModel.isPlaying.take(3).toList()
        Assert.assertEquals(listOf(false, true, false), isPlayingValues)
    }

    @Test(timeout = 2_000)
    fun isLoading() = runTest {
        every { player.isLoading } returns false
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)
        launch {
            delay(100)
            playerListenerCommander.onIsLoadingChanged(true)
            delay(100)
            playerListenerCommander.onIsLoadingChanged(false)
        }

        val isLoadingValues = viewModel.isLoading.take(3).toList()
        Assert.assertEquals(listOf(false, true, false), isLoadingValues)
    }

    @Test(timeout = 5_000)
    fun playbackState() = runTest {
        every { player.playbackState } returns Player.STATE_IDLE
        val playbackStates = listOf(Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY, Player.STATE_BUFFERING, Player.STATE_ENDED)
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)
        launch {
            for (state in playbackStates) {
                playerListenerCommander.onPlaybackStateChanged(state)
                delay(100)
            }
        }

        val playbackStateValues = viewModel.playbackState.take(playbackStates.size).toList()
        Assert.assertEquals(playbackStates, playbackStateValues)
    }

    @Test(timeout = 2_000)
    fun duration() = runTest {
        val durations = listOf(C.TIME_UNSET, 30_000, 40_000)
        every { player.duration } returnsMany durations
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)
        launch {
            delay(100)
            playerListenerCommander.onPlaybackStateChanged(Player.STATE_READY)
            delay(100)
            playerListenerCommander.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)
            delay(100)
            playerListenerCommander.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        }

        val durationValues = viewModel.duration.take(durations.size).toList()
        Assert.assertEquals(durations, durationValues)
    }

    @Test(timeout = 2_000)
    fun playerError() = runTest {
        val errors = listOf(null, mockk<PlaybackException>(), null)
        every { player.playerError } returnsMany errors
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)
        launch {
            for (error in errors) {
                delay(100)
                error?.let { playerListenerCommander.onPlayerError(it) }
                playerListenerCommander.onPlayerErrorChanged(error)
            }
        }

        val errorValues = viewModel.playerError.take(errors.size).toList()
        Assert.assertEquals(errors, errorValues)
    }

    @Test(timeout = 2_000)
    fun availableCommand() = runTest {
        val command1 = mockk<Player.Commands>()
        val command2 = mockk<Player.Commands>()
        val availableCommands = listOf(command1, command2)
        every { player.availableCommands } returnsMany availableCommands
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)
        launch {
            for (command in availableCommands) {
                delay(100)
                playerListenerCommander.onAvailableCommandsChanged(command)
            }
        }

        val availableCommandValues = viewModel.availableCommands.take(availableCommands.size).toList()
        Assert.assertEquals(availableCommands, availableCommandValues)
    }

    @Test(timeout = 5_000)
    fun updateCurrentPositionAfterSeek() = runTest {
        val positions = listOf(C.TIME_UNSET, 0L, 1000L, 2000L)
        every { player.currentPosition } returnsMany positions
        every { player.isPlaying } returns false
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)
        launch {
            for (position in positions) {
                delay(100)
                playerListenerCommander.onPositionDiscontinuity(
                    mockk(),
                    mockk(),
                    Player.DISCONTINUITY_REASON_SEEK
                )
            }
        }

        val currentPositionValues = viewModel.currentPosition.take(positions.size).toList()
        Assert.assertEquals(positions, currentPositionValues)
    }

    @Test(timeout = 5_000)
    fun currentPositionPeriodicUpdateWhilePlaying() = runTest {
        val positions = listOf(C.TIME_UNSET, 0L, 1000L, 2000L)
        every { player.currentPosition } returnsMany positions
        every { player.isPlaying } returns true
        val playerListenerCommander = PlayerListenerCommander(player)
        val viewModel = PlayerState(playerListenerCommander)
        Assert.assertTrue(playerListenerCommander.hasListeners)

        val currentPositionValues = viewModel.currentPosition.take(positions.size).toList()
        Assert.assertEquals(positions, currentPositionValues)
    }
}
