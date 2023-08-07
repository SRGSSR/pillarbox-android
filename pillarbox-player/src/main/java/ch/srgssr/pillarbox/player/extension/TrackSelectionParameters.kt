/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters

/**
 * Is text track disabled
 */
val TrackSelectionParameters.isTextTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)

/**
 * Is audio track disabled
 */
val TrackSelectionParameters.isAudioTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_AUDIO)

/**
 * Is text track default, if text tracks doesn't have overrides.
 */
val TrackSelectionParameters.isTextTrackDefault: Boolean
    get() = hasTrackOverride(C.TRACK_TYPE_TEXT)

/**
 * Is audio track default, if audio tracks doesn't have overrides.
 */
val TrackSelectionParameters.isAudioTrackDefault: Boolean
    get() = hasTrackOverride(C.TRACK_TYPE_TEXT)

/**
 * Get overrides for track type
 *
 * @param trackType The track type to filter.
 * @return
 */
fun TrackSelectionParameters.getOverridesForTrackType(trackType: @TrackType Int): Map<TrackGroup, TrackSelectionOverride> {
    return overrides.filterKeys { it.type == trackType }
}

/**
 * Has track override
 *
 * @param trackType The track type to filter.
 * @return
 */
fun TrackSelectionParameters.hasTrackOverride(trackType: @TrackType Int): Boolean {
    if (overrides.isEmpty()) return false
    return getOverridesForTrackType(trackType).isNotEmpty()
}
