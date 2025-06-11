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
 * Selects the provided [track] for playback.
 *
 * @param track The [Track] to select.
 */
fun Player.selectTrack(track: Track) {
    val trackGroup = currentTracks.groups[track.groupIndex].mediaTrackGroup

    setTrackOverride(TrackSelectionOverride(trackGroup, track.trackIndexInGroup))
}

/**
 * Enables the audio track.
 */
fun Player.enableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.enableAudioTrack()
}

/**
 * Enables the text track.
 */
fun Player.enableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.enableTextTrack()
}

/**
 * Enables the video track.
 */
fun Player.enableVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.enableVideoTrack()
}

/**
 * Disables the audio track.
 */
fun Player.disableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.disableAudioTrack()
}

/**
 * Disables the text track.
 */
fun Player.disableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.disableTextTrack()
}

/**
 * Disables the video track.
 */
fun Player.disableVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.disableVideoTrack()
}

/**
 * Sets the track selection to automatically select the default audio track.
 *
 * @param context The [Context].
 */
fun Player.setAutoAudioTrack(context: Context? = null) {
    trackSelectionParameters = trackSelectionParameters.defaultAudioTrack(context)
}

/**
 * Sets the track selection to automatically select the default text track.
 */
fun Player.setAutoTextTrack() {
    trackSelectionParameters = trackSelectionParameters.defaultTextTrack()
}

/**
 * Sets the track selection to automatically select the default video track.
 */
fun Player.setAutoVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.defaultVideoTrack()
}
