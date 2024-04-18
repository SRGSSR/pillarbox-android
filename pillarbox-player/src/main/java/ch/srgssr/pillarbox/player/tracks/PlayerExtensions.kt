/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import android.content.Context
import androidx.media3.common.C
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
import ch.srgssr.pillarbox.player.extension.isVideoTrackDisabled
import ch.srgssr.pillarbox.player.extension.setTrackOverride

/**
 * All the supported video qualities for the currently played [MediaItem][androidx.media3.common.MediaItem].
 */
val Player.videoQualities: List<VideoQuality>
    get() {
        val isVideoTrackDisabled = trackSelectionParameters.isVideoTrackDisabled
        val filteredVideoTracks = currentTracks.videoTracks
            .filter { isVideoTrackDisabled || it.group.isSelected }
            .distinctBy { it.format.height }
            .sortedByDescending { it.format.height }
            .filter { it.format.height > 0 && it.format.width > 0 }

        val preferredFormat = if (isVideoTrackDisabled) {
            null
        } else {
            val maxVideoHeight = trackSelectionParameters.maxVideoHeight.takeIf { it != Int.MAX_VALUE }
            val maxVideoWidth = trackSelectionParameters.maxVideoWidth.takeIf { it != Int.MAX_VALUE }

            filteredVideoTracks
                .filter { it.isSelected }
                .firstOrNull { videoTrack ->
                    val format = videoTrack.format
                    val expectedHeight = maxVideoHeight ?: format.height
                    val expectedWidth = maxVideoWidth ?: format.width

                    format.height <= expectedHeight && format.width <= expectedWidth
                }
                ?.format
        }

        return filteredVideoTracks.map { videoTrack ->
            VideoQuality(
                format = videoTrack.format,
                isSelected = videoTrack.format == preferredFormat,
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
 * Select the max video quality that this [Player] should try to play.
 *
 * @param videoQuality The max video quality.
 */
fun Player.selectMaxVideoQuality(videoQuality: VideoQuality) {
    trackSelectionParameters = trackSelectionParameters.buildUpon()
        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
        .setMaxVideoSize(videoQuality.width, videoQuality.height)
        .build()
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
 */
fun Player.setAutoVideoTrack() {
    trackSelectionParameters = trackSelectionParameters.defaultVideoTrack()
}
