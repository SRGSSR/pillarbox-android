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
 * FIXME Doesn't work in case of playlists. A overrides can be setup but no applicable for the next item.
 */
val TrackSelectionParameters.isTextTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_TEXT) ||
        isForcedTextTrackActive

/**
 * Is audio track disabled
 */
val TrackSelectionParameters.isAudioTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_AUDIO)

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
 * Disable text track, forced subtitles may be shown.
 *
 * To completely remove text track [TrackSelectionParameters.disabledTrackTypes]
 *
 * @return
 */
fun TrackSelectionParameters.disableTextTrack(): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
        .setPreferredTextRoleFlags(0)
        .setPreferredTextLanguage(null)
        .setPreferredTextRoleFlags(0)
        .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_FORCED.inv())
        .build()
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
 * Reset [TrackSelectionParameters] for text as Default.
 *
 * @param context
 * @return
 */
fun TrackSelectionParameters.defaultTextTrack(context: Context): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
        .setIgnoredTextSelectionFlags(0)
        .setPreferredTextLanguage(null)
        .setPreferredTextRoleFlags(0)
        .setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(context)
        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
        .build()
}

/**
 * Default audio track
 *
 * Reset [TrackSelectionParameters] for audio as Default.
 *
 * @param context
 * @return
 */
fun TrackSelectionParameters.defaultAudioTrack(context: Context): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
        .setPreferredAudioLanguage(null)
        .setPreferredAudioMimeType(null)
        .setPreferredAudioRoleFlags(0)
        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
        .build()
}

/**
 * Set track selection override
 *
 * Audio track selection override also setups the preferred audio language to handle forced subtitles correctly.
 *
 * @param override The [TrackSelectionOverride] to apply.
 * @return
 */
fun TrackSelectionParameters.setTrackOverride(override: TrackSelectionOverride): TrackSelectionParameters {
    val builder = buildUpon()
        .setOverrideForType(override)
        .setTrackTypeDisabled(override.type, false)

    // If audio and has tracks to select then set preferred language if applicable.
    return when {
        override.type == C.TRACK_TYPE_AUDIO && override.trackIndices.isNotEmpty() -> {
            builder.setPreferredAudioLanguage(override.mediaTrackGroup.getFormat(0).language)
            builder.build()
        }

        override.type == C.TRACK_TYPE_TEXT && override.trackIndices.isNotEmpty() -> {
            builder.setIgnoredTextSelectionFlags(0)
            builder.setPreferredTextLanguage(override.mediaTrackGroup.getFormat(0).language)
            builder.setPreferredTextRoleFlags(override.mediaTrackGroup.getFormat(0).roleFlags)
            builder.build()
        }

        else -> {
            builder.build()
        }
    }
}

private fun TrackSelectionParameters.disableTrackType(trackType: @TrackType Int): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(trackType)
        .setTrackTypeDisabled(trackType, true)
        .build()
}

private val TrackSelectionParameters.isForcedTextTrackActive: Boolean
    get() = ignoredTextSelectionFlags == C.SELECTION_FLAG_FORCED.inv()
