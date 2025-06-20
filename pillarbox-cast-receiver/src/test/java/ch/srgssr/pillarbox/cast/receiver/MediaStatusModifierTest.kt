/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.cast.receiver.extensions.setPlaybackRateFromPlaybackParameter
import ch.srgssr.pillarbox.cast.receiver.extensions.setSupportedMediaCommandsFromAvailableCommand
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.tv.media.MediaStatusModifier
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MediaStatusModifierTest {

    @Test
    fun `check playback rate value`() {
        val playbackRates = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f)
        playbackRates.forEachIndexed { index, rate ->
            val mediaStatusModifier = MediaStatusModifier()
            val playbackParameters = PlaybackParameters(rate)
            mediaStatusModifier.setPlaybackRateFromPlaybackParameter(playbackParameters)
            assertEquals(playbackRates[index], mediaStatusModifier.playbackRate?.toFloat())
        }
    }

    @Test
    fun `check supported media commands when Player commands are available`() {
        val commandWithPlaybackRate = Player.Commands.Builder()
            .add(Player.COMMAND_PLAY_PAUSE)
            .add(Player.COMMAND_RELEASE)
            .add(Player.COMMAND_SET_SPEED_AND_PITCH)
            .add(Player.COMMAND_SET_REPEAT_MODE)
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
            .add(Player.COMMAND_GET_TRACKS)
            .build()

        val mediaStatusModifier = MediaStatusModifier()
        mediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(commandWithPlaybackRate)
        val expectedCommands = mapOf(
            MediaStatus.COMMAND_PLAYBACK_RATE to true,
            MediaStatus.COMMAND_QUEUE_SHUFFLE to true,
            MediaStatus.COMMAND_EDIT_TRACKS to true,
            MediaStatus.COMMAND_QUEUE_REPEAT_ONE to true,
            MediaStatus.COMMAND_QUEUE_REPEAT_ALL to true,
        )
        val supportedCommand = mediaStatusModifier.supportedMediaCommandOverride

        assertEquals(expectedCommands.toSortedMap(), supportedCommand.toSortedMap())
    }
}
