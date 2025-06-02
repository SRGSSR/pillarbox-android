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
import kotlin.test.assertTrue

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
            .add(Player.COMMAND_SET_SPEED_AND_PITCH)
            .build()

        val mediaStatusModifier = MediaStatusModifier()
        mediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(commandWithPlaybackRate)
        assertTrue(mediaStatusModifier.supportedMediaCommandOverride[MediaStatus.COMMAND_PLAYBACK_RATE] == true, "command is enabled")
    }

    @Test
    fun `check supported media commands when Player commands are not available`() {
        val commandWithPlaybackRate = Player.Commands.Builder()
            .add(Player.COMMAND_PLAY_PAUSE)
            .build()

        val mediaStatusModifier = MediaStatusModifier()
        mediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(commandWithPlaybackRate)
        assertTrue(mediaStatusModifier.supportedMediaCommandOverride[MediaStatus.COMMAND_PLAYBACK_RATE] == false)
    }
}
