/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver.extensions

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SET_SPEED_AND_PITCH
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.tv.media.MediaStatusModifier

internal fun MediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(availableCommands: Player.Commands) {
    setMediaCommandSupported(
        MediaStatus.COMMAND_PLAYBACK_RATE,
        availableCommands.contains(COMMAND_SET_SPEED_AND_PITCH)
    )
}

internal fun MediaStatusModifier.setPlaybackRateFromPlaybackParameter(playbackParameters: PlaybackParameters) {
    playbackRate = playbackParameters.speed.toDouble()
}
