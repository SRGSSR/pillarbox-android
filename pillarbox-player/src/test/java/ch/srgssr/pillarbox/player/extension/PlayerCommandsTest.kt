/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.Player
import androidx.media3.common.Player.Commands
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PlayerCommandsTest {
    @Test
    fun `can seek to next`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canSeekToNext())
        assertFalse(player.availableCommands.canSeekToNext())
        assertTrue(player.availableCommands.canSeekToNext())
    }

    @Test
    fun `can seek to previous`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canSeekToPrevious())
        assertFalse(player.availableCommands.canSeekToPrevious())
        assertTrue(player.availableCommands.canSeekToPrevious())
    }

    @Test
    fun `can seek forward`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SEEK_FORWARD, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canSeekForward())
        assertFalse(player.availableCommands.canSeekForward())
        assertTrue(player.availableCommands.canSeekForward())
    }

    @Test
    fun `can seek back`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SEEK_BACK, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canSeekBack())
        assertFalse(player.availableCommands.canSeekBack())
        assertTrue(player.availableCommands.canSeekBack())
    }

    @Test
    fun `can seek`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canSeek())
        assertFalse(player.availableCommands.canSeek())
        assertTrue(player.availableCommands.canSeek())
    }

    @Test
    fun `can play pause`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_PLAY_PAUSE, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canPlayPause())
        assertFalse(player.availableCommands.canPlayPause())
        assertTrue(player.availableCommands.canPlayPause())
    }

    @Test
    fun `can get tracks`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TRACKS, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canGetTracks())
        assertFalse(player.availableCommands.canGetTracks())
        assertTrue(player.availableCommands.canGetTracks())
    }

    @Test
    fun `can set track selection parameters`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canSetTrackSelectionParameters())
        assertFalse(player.availableCommands.canSetTrackSelectionParameters())
        assertTrue(player.availableCommands.canSetTrackSelectionParameters())
    }

    @Test
    fun `can speed and pitch`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_GET_TEXT).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SET_SPEED_AND_PITCH, Player.COMMAND_GET_TEXT).build()
            )
        }

        assertFalse(player.availableCommands.canSpeedAndPitch())
        assertFalse(player.availableCommands.canSpeedAndPitch())
        assertTrue(player.availableCommands.canSpeedAndPitch())
    }

    @Test
    fun `can set shuffle mode`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SET_SHUFFLE_MODE).build()
            )
        }

        assertFalse(player.availableCommands.canSetShuffleMode())
        assertFalse(player.availableCommands.canSetShuffleMode())
        assertTrue(player.availableCommands.canSetShuffleMode())
    }

    @Test
    fun `can set repeat mode`() {
        val player = mockk<Player> {
            every { availableCommands } returnsMany listOf(
                Commands.Builder().build(),
                Commands.Builder().addAll(Player.COMMAND_STOP).build(),
                Commands.Builder().addAll(Player.COMMAND_STOP, Player.COMMAND_SET_REPEAT_MODE).build()
            )
        }

        assertFalse(player.availableCommands.canSetRepeatMode())
        assertFalse(player.availableCommands.canSetRepeatMode())
        assertTrue(player.availableCommands.canSetRepeatMode())
    }
}
