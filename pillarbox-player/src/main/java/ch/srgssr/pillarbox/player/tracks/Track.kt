/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import androidx.media3.common.C.TRACK_TYPE_AUDIO
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.C.TRACK_TYPE_VIDEO
import androidx.media3.common.Format
import androidx.media3.common.Tracks

sealed class Track(
    val format: Format,
    internal val groupIndex: Int,
    internal val trackIndexInGroup: Int,
    internal val isSupported: Boolean,
) {
    companion object {
        operator fun invoke(
            group: Tracks.Group,
            groupIndex: Int,
            trackIndexInGroup: Int,
        ): Track? {
            val trackConstructor = when (group.type) {
                TRACK_TYPE_AUDIO -> ::AudioTrack
                TRACK_TYPE_TEXT -> ::TextTrack
                TRACK_TYPE_VIDEO -> ::VideoTrack
                else -> null
            }

            return trackConstructor?.invoke(
                group.getTrackFormat(trackIndexInGroup),
                groupIndex,
                trackIndexInGroup,
                group.isTrackSupported(trackIndexInGroup),
            )
        }
    }
}

class AudioTrack(
    format: Format,
    groupIndex: Int,
    trackIndexInGroup: Int,
    isSupported: Boolean,
) : Track(
    format = format,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
    isSupported = isSupported,
)

class TextTrack(
    format: Format,
    groupIndex: Int,
    trackIndexInGroup: Int,
    isSupported: Boolean,
) : Track(
    format = format,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
    isSupported = isSupported,
)

class VideoTrack(
    format: Format,
    groupIndex: Int,
    trackIndexInGroup: Int,
    isSupported: Boolean,
) : Track(
    format = format,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
    isSupported = isSupported,
)
