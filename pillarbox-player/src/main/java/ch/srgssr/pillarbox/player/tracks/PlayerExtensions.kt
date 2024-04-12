/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import ch.srgssr.pillarbox.player.extension.defaultAudioTrack
import ch.srgssr.pillarbox.player.extension.defaultTextTrack
import ch.srgssr.pillarbox.player.extension.defaultVideoTrack
import ch.srgssr.pillarbox.player.extension.disableAudioTrack
import ch.srgssr.pillarbox.player.extension.disableTextTrack
import ch.srgssr.pillarbox.player.extension.disableVideoTrack
import ch.srgssr.pillarbox.player.extension.enableAudioTrack
import ch.srgssr.pillarbox.player.extension.enableTextTrack
import ch.srgssr.pillarbox.player.extension.enableVideoTrack
import ch.srgssr.pillarbox.player.extension.setTrackOverride

fun Player.selectTrack(track: Track) {
    val trackGroup = currentTracks.groups[track.groupIndex].mediaTrackGroup

    setTrackOverride(TrackSelectionOverride(trackGroup, track.trackIndexInGroup))
}

fun Player.selectTracks(tracks: List<VideoTrack>) {
    if (tracks.isEmpty()) {
        return
    }

    if (tracks.size == 1) {
        return selectTrack(tracks[0])
    }

    val groupIndex = tracks[0].groupIndex
    if (tracks.any { it.groupIndex != groupIndex }) {
        return
    }

    val trackGroup = currentTracks.groups[groupIndex].mediaTrackGroup
    val trackIndices = tracks.map { it.trackIndexInGroup }

    setTrackOverride(TrackSelectionOverride(trackGroup, trackIndices))
}

fun Player.enableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.enableAudioTrack()
}

fun Player.enableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.enableTextTrack()
}

fun Player.enableVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.enableVideoTrack()
}

fun Player.disableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.disableAudioTrack()
}

fun Player.disableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.disableTextTrack()
}

fun Player.disableVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.disableVideoTrack()
}

fun Player.setAutoAudioTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultAudioTrack(context)
}

fun Player.setAutoTextTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultTextTrack(context)
}

fun Player.setAutoVideoTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultVideoTrack(context)
}
