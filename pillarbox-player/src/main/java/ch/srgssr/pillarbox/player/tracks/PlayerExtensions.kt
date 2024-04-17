/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
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
 * All the supported video qualities for the currently played [MediaItem][androidx.media3.common.MediaItem].
 */
val Player.videoQualities: List<VideoQualityTrack>
    @SuppressLint("WrongConstant")
    get() {
        val maxVideoHeight = trackSelectionParameters.maxVideoHeight
        val maxVideoWidth = trackSelectionParameters.maxVideoWidth
        val filteredVideoTracks = currentTracks.videoTracks
            .distinctBy { it.format.height }
            .sortedByDescending { it.format.height }
            .filter { it.format.height > 0 && it.format.width > 0 }

        val preferredFormat = filteredVideoTracks.firstOrNull { videoTrack ->
            val format = videoTrack.format
            val expectedHeight = maxVideoHeight.takeIf { it != Int.MAX_VALUE } ?: format.height
            val expectedWidth = maxVideoWidth.takeIf { it != Int.MAX_VALUE } ?: format.width

            format.height <= expectedHeight && format.width <= expectedWidth
        }?.format

        val trackSupport: IntArray
        val trackSelected: BooleanArray

        if (filteredVideoTracks.isEmpty()) {
            trackSupport = intArrayOf()
            trackSelected = booleanArrayOf()
        } else {
            val videoTrackGroup = filteredVideoTracks[0].group
            val groupLength = videoTrackGroup.length

            trackSupport = IntArray(groupLength)
            trackSelected = BooleanArray(groupLength)

            repeat(groupLength) { trackIndex ->
                trackSupport[trackIndex] = videoTrackGroup.getTrackSupport(trackIndex)
                trackSelected[trackIndex] = videoTrackGroup.getTrackFormat(trackIndex) == preferredFormat
            }
        }

        return filteredVideoTracks.map { videoTrack ->
            VideoQualityTrack(
                group = Tracks.Group(
                    videoTrack.group.mediaTrackGroup,
                    videoTrack.group.isAdaptiveSupported,
                    trackSupport,
                    trackSelected,
                ),
                groupIndex = videoTrack.groupIndex,
                trackIndexInGroup = videoTrack.trackIndexInGroup,
            )
        }
    }

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
