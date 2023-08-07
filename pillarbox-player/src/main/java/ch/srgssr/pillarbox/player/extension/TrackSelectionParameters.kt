/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:SuppressWarnings("UnusedPrivateMember")

package ch.srgssr.pillarbox.player.extension

import android.content.Context
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
    get() = hasTrackOverride(C.TRACK_TYPE_AUDIO)

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

/**
 * Disable text track
 *
 * @return
 */
fun TrackSelectionParameters.disableTextTrack(): TrackSelectionParameters {
    return disableTrackType(C.TRACK_TYPE_TEXT)
}

/**
 * Disable audio track
 *
 * @return
 */
fun TrackSelectionParameters.disableAudioTrack(): TrackSelectionParameters {
    return disableTrackType(C.TRACK_TYPE_AUDIO)
}

/**
 * Default text track
 *
 * @param context
 * @return
 */
fun TrackSelectionParameters.defaultTextTrack(context: Context): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
        .setIgnoredTextSelectionFlags(0)
        .setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(context)
        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
        .build()
}

/**
 * Default audio track
 *
 * @param context
 * @return
 */
fun TrackSelectionParameters.defaultAudioTrack(context: Context): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
        .build()
}

/**
 * Set track override
 *
 * Track override the automatic selection from TrackSelector. It can messing up the selection of forced subtitles in case you override audio
 * selection.
 *
 * @param override The [TrackSelectionOverride] to apply.
 * @return
 */
fun TrackSelectionParameters.setTrackOverride(override: TrackSelectionOverride): TrackSelectionParameters {
    return buildUpon()
        .setOverrideForType(override)
        .setTrackTypeDisabled(override.type, false)
        .build()
}

private fun TrackSelectionParameters.disableTrackType(trackType: @TrackType Int): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(trackType)
        .setTrackTypeDisabled(trackType, true)
        .build()
}
