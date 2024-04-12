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
    private val group: Tracks.Group,
    internal val groupIndex: Int,
    internal val trackIndexInGroup: Int,
) {
    val format: Format
        get() = group.getTrackFormat(trackIndexInGroup)

    val isSelected: Boolean
        get() = group.isTrackSelected(trackIndexInGroup)

    val isSupported: Boolean
        get() = group.isTrackSupported(trackIndexInGroup)

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
                group,
                groupIndex,
                trackIndexInGroup,
            )
        }
    }
}

class AudioTrack(
    group: Tracks.Group,
    groupIndex: Int,
    trackIndexInGroup: Int,
) : Track(
    group = group,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
)

class TextTrack(
    group: Tracks.Group,
    groupIndex: Int,
    trackIndexInGroup: Int,
) : Track(
    group = group,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
)

class VideoTrack(
    group: Tracks.Group,
    groupIndex: Int,
    trackIndexInGroup: Int,
) : Track(
    group = group,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
)
