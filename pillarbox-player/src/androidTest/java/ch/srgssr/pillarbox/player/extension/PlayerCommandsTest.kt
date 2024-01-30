/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.Player
import androidx.media3.common.Player.Commands
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCommandsTest {
    @Test
    fun canSeekToNext() {
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
    fun canSeekToPrevious() {
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
    fun canSeekForward() {
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
    fun canSeekBack() {
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
    fun canSeek() {
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
    fun canPlayPause() {
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
    fun canGetTracks() {
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
    fun canSetTrackSelectionParameters() {
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
    fun canSpeedAndPitch() {
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
}
