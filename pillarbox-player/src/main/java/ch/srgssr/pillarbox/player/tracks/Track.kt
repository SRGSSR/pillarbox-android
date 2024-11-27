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

/**
 * Represents a generic track within a media stream.
 *
 * @property group The [Tracks.Group] that contains this track.
 * @property groupIndex The index of the containing [Tracks.Group] within the overall track list.
 * @property trackIndexInGroup The index of this track within its containing [Tracks.Group].
 */
sealed class Track(
    internal val group: Tracks.Group,
    internal val groupIndex: Int,
    internal val trackIndexInGroup: Int,
) {
    /**
     * The [Format] of this [Track].
     */
    val format: Format
        get() = group.getTrackFormat(trackIndexInGroup)

    /**
     * Indicates whether this [Track] is currently selected.
     */
    val isSelected: Boolean
        get() = group.isTrackSelected(trackIndexInGroup)

    /**
     * Indicates whether this [Track] is supported for playback.
     */
    val isSupported: Boolean
        get() = group.isTrackSupported(trackIndexInGroup)

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Creates a [Track] from the track at [trackIndexInGroup] in [group].
         *
         * @param group The [Tracks.Group] containing the track to convert.
         * @param groupIndex The index of the containing [Tracks.Group].
         * @param trackIndexInGroup The index of the track within its containing [Tracks.Group].
         * @return A [Track] representing the desired track, or `null` if the [Tracks.Group.type] is not supported.
         */
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

/**
 * Represents an audio track within a media file.
 *
 * @param group The [Group][Tracks.Group] this audio track belongs to.
 * @param groupIndex The index of the group this track belongs to within the overall track list.
 * @param trackIndexInGroup The index of this track within its containing group.
 */
class AudioTrack(
    group: Tracks.Group,
    groupIndex: Int,
    trackIndexInGroup: Int,
) : Track(
    group = group,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
)

/**
 * Represents a text track within a media file.
 *
 * @param group The [Group][Tracks.Group] this audio track belongs to.
 * @param groupIndex The index of the group this track belongs to within the overall track list.
 * @param trackIndexInGroup The index of this track within its containing group.
 */
class TextTrack(
    group: Tracks.Group,
    groupIndex: Int,
    trackIndexInGroup: Int,
) : Track(
    group = group,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
)

/**
 * Represents a video track within a media file.
 *
 * @param group The [Group][Tracks.Group] this audio track belongs to.
 * @param groupIndex The index of the group this track belongs to within the overall track list.
 * @param trackIndexInGroup The index of this track within its containing group.
 */
class VideoTrack(
    group: Tracks.Group,
    groupIndex: Int,
    trackIndexInGroup: Int,
) : Track(
    group = group,
    groupIndex = groupIndex,
    trackIndexInGroup = trackIndexInGroup,
)
