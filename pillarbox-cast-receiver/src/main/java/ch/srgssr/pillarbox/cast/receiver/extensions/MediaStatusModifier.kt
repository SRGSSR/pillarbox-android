/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver.extensions

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SET_SPEED_AND_PITCH
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.cast.extension.toMediaTrack
import ch.srgssr.pillarbox.player.tracks.tracks
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.tv.media.MediaStatusModifier

internal fun MediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(availableCommands: Player.Commands) {
    setMediaCommandSupported(
        MediaStatus.COMMAND_PLAYBACK_RATE,
        availableCommands.contains(COMMAND_SET_SPEED_AND_PITCH)
    )
    setMediaCommandSupported(
        MediaStatus.COMMAND_EDIT_TRACKS,
        availableCommands.contains(Player.COMMAND_GET_TRACKS) && availableCommands.contains(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
    )
    // COMMAND_QUEUE_REPEAT set COMMAND_QUEUE_REPEAT_ONE and COMMAND_QUEUE_REPEAT_ALL. But not COMMAND_QUEUE_REPEAT.
    setMediaCommandSupported(MediaStatus.COMMAND_QUEUE_REPEAT, availableCommands.contains(Player.COMMAND_SET_REPEAT_MODE))
    setMediaCommandSupported(MediaStatus.COMMAND_QUEUE_SHUFFLE, availableCommands.contains(Player.COMMAND_SET_SHUFFLE_MODE))
}

internal fun MediaStatusModifier.setPlaybackRateFromPlaybackParameter(playbackParameters: PlaybackParameters) {
    playbackRate = playbackParameters.speed.toDouble()
}

internal fun MediaStatusModifier.setMediaTracksFromTracks(tracks: Tracks) {
    val listTracks = mutableListOf<MediaTrack>()
    val listSelectedTracks = mutableListOf<Long>()
    tracks.tracks.forEachIndexed { index, track ->
        val trackId = index.toLong()
        listTracks.add(track.toMediaTrack(trackId))
        if (track.isSelected) listSelectedTracks.add(trackId)
    }
    mediaInfoModifier?.mediaTracks = listTracks
    mediaTracksModifier.setActiveTrackIds(listSelectedTracks.toLongArray())
}
