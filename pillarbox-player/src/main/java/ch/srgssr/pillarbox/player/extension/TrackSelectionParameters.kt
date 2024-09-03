/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters

/**
 * Is text track disabled
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
 * Is video track disabled
 */
val TrackSelectionParameters.isVideoTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_VIDEO)

/**
 * Get overrides for track type
 *
 * @param trackType The track type to filter.
 * @return
 */
fun TrackSelectionParameters.getOverridesForTrackType(
    trackType: @TrackType Int
): Map<TrackGroup, TrackSelectionOverride> {
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
 * Enable text track.
 *
 * @return
 */
fun TrackSelectionParameters.enableTextTrack(): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
        .setPreferredTextRoleFlags(0)
        .setPreferredTextLanguage(null)
        .setIgnoredTextSelectionFlags(0)
        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
        .build()
}

/**
 * Enable audio track.
 *
 * @return
 */
fun TrackSelectionParameters.enableAudioTrack(): TrackSelectionParameters {
    return enableTrackType(C.TRACK_TYPE_AUDIO)
}

/**
 * Enable video track.
 *
 * @return
 */
fun TrackSelectionParameters.enableVideoTrack(): TrackSelectionParameters {
    return enableTrackType(C.TRACK_TYPE_VIDEO)
}

/**
 * Disable text track
 *
 * @return
 */
fun TrackSelectionParameters.disableTextTrack(): TrackSelectionParameters {
    @Suppress("WrongConstant") // Lint embedded in AGP 8.6.0 doesn't seem to recognize inverted flags in setIgnoredTextSelectionFlags()
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
        .setPreferredTextRoleFlags(0)
        .setPreferredTextLanguage(null)
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
 * Disable video track
 *
 * @return
 */
fun TrackSelectionParameters.disableVideoTrack(): TrackSelectionParameters {
    return disableTrackType(C.TRACK_TYPE_VIDEO)
}

/**
 * Default text track parameters.
 *
 * @param context The context.
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
 * Default audio track parameters.
 *
 * Reset [TrackSelectionParameters] for audio as Default.
 *
 * @param context The context.
 * @return
 */
fun TrackSelectionParameters.defaultAudioTrack(context: Context): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
        .setPreferredAudioLanguage(null)
        .setPreferredAudioMimeType(null)
        .setPreferredAudioRoleFlags(0)
        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
        .setPreferredAudioRoleFlagsToAccessibilityManagerSettings(context)
        .build()
}

/**
 * Default video track parameters.
 *
 * Reset [TrackSelectionParameters] for video as Default.
 *
 * @return
 */
fun TrackSelectionParameters.defaultVideoTrack(): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
        .setPreferredVideoMimeType(null)
        .setPreferredVideoRoleFlags(0)
        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
        .setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
        .build()
}

/**
 * Set track selection override
 *
 * - Audio track selection override setups the preferred audio language to handle forced subtitles correctly.
 * - Text track selection override setups the preferred text language.
 * - Video track selection override setups the max video size.
 *
 * @param override The [TrackSelectionOverride] to apply.
 * @return
 */
fun TrackSelectionParameters.setTrackOverride(override: TrackSelectionOverride): TrackSelectionParameters {
    val builder = buildUpon()
        .setTrackTypeDisabled(override.type, false)

    if (override.trackIndices.isEmpty()) {
        return builder.build()
    }

    val format = override.mediaTrackGroup.getFormat(override.trackIndices[0])
    builder.setOverrideForType(override)
    when (override.type) {
        C.TRACK_TYPE_AUDIO -> {
            builder.setPreferredAudioLanguage(format.language)
        }

        C.TRACK_TYPE_TEXT -> {
            builder.setIgnoredTextSelectionFlags(0)
            builder.setPreferredTextLanguage(format.language)
            builder.setPreferredTextRoleFlags(format.roleFlags)
        }

        C.TRACK_TYPE_VIDEO -> {
            builder.setMaxVideoSize(format.width, format.height)
        }
    }

    return builder.build()
}

/**
 * Set preferred audio role flags to accessibility manager settings.
 *
 * Dos nothing for api level < 33 or when audio description request is off.
 * @param context The context.
 */
fun TrackSelectionParameters.Builder.setPreferredAudioRoleFlagsToAccessibilityManagerSettings(
    context: Context
): TrackSelectionParameters.Builder {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        setPreferredAudioRoleFlagsToAccessibilityManagerSettingsV33(context)
    }
    return this
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun TrackSelectionParameters.Builder.setPreferredAudioRoleFlagsToAccessibilityManagerSettingsV33(
    context: Context
) {
    val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
    if (accessibilityManager.isAudioDescriptionRequested) {
        setPreferredAudioRoleFlags(C.ROLE_FLAG_DESCRIBES_VIDEO or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND)
    }
}

private fun TrackSelectionParameters.enableTrackType(trackType: @TrackType Int): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(trackType)
        .setTrackTypeDisabled(trackType, false)
        .build()
}

private fun TrackSelectionParameters.disableTrackType(trackType: @TrackType Int): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(trackType)
        .setTrackTypeDisabled(trackType, true)
        .build()
}

private val TrackSelectionParameters.isForcedTextTrackActive: Boolean
    get() = ignoredTextSelectionFlags == C.SELECTION_FLAG_FORCED.inv()
