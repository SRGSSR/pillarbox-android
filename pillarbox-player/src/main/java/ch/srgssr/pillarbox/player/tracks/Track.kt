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
 * Generic wrapper a of track.
 *
 * @property group The [Group][Tracks.Group] containing this [Track].
 * @property groupIndex The index of the containing [Group][Tracks.Group].
 * @property trackIndexInGroup The index of this [Track] in its containing [Group][Tracks.Group].
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
     * `true` if this [Track] is selected, `false` otherwise.
     */
    val isSelected: Boolean
        get() = group.isTrackSelected(trackIndexInGroup)

    companion object {
        /**
         * Converts the track at index [trackIndexInGroup] from the provided [group] into a [Track].
         *
         * @param group The [Group][Tracks.Group] containing the track to convert.
         * @param groupIndex The index of the containing [Group][Tracks.Group].
         * @param trackIndexInGroup The index of this [Track] in its containing [Group][Tracks.Group].
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
 * Represent an audio track.
 *
 * @param group The [Group][Tracks.Group] containing this [Track].
 * @param groupIndex The index of the containing [Group][Tracks.Group].
 * @param trackIndexInGroup The index of this [Track] in its containing [Group][Tracks.Group].
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
 * Represent a text track.
 *
 * @param group The [Group][Tracks.Group] containing this [Track].
 * @param groupIndex The index of the containing [Group][Tracks.Group].
 * @param trackIndexInGroup The index of this [Track] in its containing [Group][Tracks.Group].
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
 * Represent a video track.
 *
 * @param group The [Group][Tracks.Group] containing this [Track].
 * @param groupIndex The index of the containing [Group][Tracks.Group].
 * @param trackIndexInGroup The index of this [Track] in its containing [Group][Tracks.Group].
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
