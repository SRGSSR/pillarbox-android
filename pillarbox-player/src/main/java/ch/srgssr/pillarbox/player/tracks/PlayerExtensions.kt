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

/**
 * Select the provided [track].
 *
 * @param track The [Track] to select.
 */
fun Player.selectTrack(track: Track) {
    val trackGroup = currentTracks.groups[track.groupIndex].mediaTrackGroup

    setTrackOverride(TrackSelectionOverride(trackGroup, track.trackIndexInGroup))
}

/**
 * Enable the audio track.
 */
fun Player.enableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.enableAudioTrack()
}

/**
 * Enable the text track.
 */
fun Player.enableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.enableTextTrack()
}

/**
 * Enable the video track.
 */
fun Player.enableVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.enableVideoTrack()
}

/**
 * Disable the audio track.
 */
fun Player.disableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.disableAudioTrack()
}

/**
 * Disable the text track.
 */
fun Player.disableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.disableTextTrack()
}

/**
 * Disable the video track.
 */
fun Player.disableVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.disableVideoTrack()
}

/**
 * Restore the default audio track.
 *
 * @param context
 */
fun Player.setAutoAudioTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultAudioTrack(context)
}

/**
 * Restore the default text track.
 *
 * @param context
 */
fun Player.setAutoTextTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultTextTrack(context)
}

/**
 * Restore the default video track.
 *
 * @param context
 */
fun Player.setAutoVideoTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultVideoTrack(context)
}
