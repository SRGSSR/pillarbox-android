/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver.extensions

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SET_SPEED_AND_PITCH
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.VideoTrack
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
}

internal fun MediaStatusModifier.setPlaybackRateFromPlaybackParameter(playbackParameters: PlaybackParameters) {
    playbackRate = playbackParameters.speed.toDouble()
}

internal fun MediaStatusModifier.setMediaTracksFromTracks(tracks: Tracks) {
    val listTracks = mutableListOf<MediaTrack>()
    val listSelectedTracks = mutableListOf<Long>()
    tracks.tracks.forEachIndexed { index, track ->
        val type = when (track) {
            is TextTrack -> MediaTrack.TYPE_TEXT
            is AudioTrack -> MediaTrack.TYPE_AUDIO
            is VideoTrack -> MediaTrack.TYPE_VIDEO
        }
        val mediaTrack = MediaTrack.Builder(index.toLong(), type)
            .setLanguage(track.format.language)
            .setContentType(if (track is TextTrack) track.format.containerMimeType else track.format.sampleMimeType)
            .setName(track.format.label)
            .setContentId(track.format.id)
            .build()
        listTracks.add(mediaTrack)
        if (track.isSelected) listSelectedTracks.add(mediaTrack.id)
    }
    mediaInfoModifier?.mediaTracks = listTracks
    mediaTracksModifier.setActiveTrackIds(listSelectedTracks.toLongArray())
}
