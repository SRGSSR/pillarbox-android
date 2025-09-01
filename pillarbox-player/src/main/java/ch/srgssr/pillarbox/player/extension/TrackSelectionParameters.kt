/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("TooManyFunctions")

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
 * Indicates whether text track is disabled.
 */
val TrackSelectionParameters.isTextTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_TEXT) ||
        isForcedTextTrackActive

/**
 * Indicates whether audio track is disabled.
 */
val TrackSelectionParameters.isAudioTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_AUDIO)

/**
 * Indicates whether video track is disabled.
 */
val TrackSelectionParameters.isVideoTrackDisabled: Boolean
    get() = disabledTrackTypes.contains(C.TRACK_TYPE_VIDEO)

/**
 * Filters the existing track selection overrides and returns only those that apply to the specified track type.
 *
 * @param trackType The type of track to filter for.
 * @return A map containing only the track selection overrides that apply to the specified `trackType`.
 */
fun TrackSelectionParameters.getOverridesForTrackType(
    trackType: @TrackType Int
): Map<TrackGroup, TrackSelectionOverride> {
    return overrides.filterKeys { it.type == trackType }
}

/**
 * Checks if there is an override for the specified track type.
 *
 * @param trackType The type of track to check for overrides.
 * @return Whether there is at least one override for the specified track type.
 */
fun TrackSelectionParameters.hasTrackOverride(trackType: @TrackType Int): Boolean {
    if (overrides.isEmpty()) return false
    return getOverridesForTrackType(trackType).isNotEmpty()
}

/**
 * Creates a new [TrackSelectionParameters] instance with text track enabled.
 *
 * @return A new [TrackSelectionParameters] instance with text track enabled.
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
 * Creates a new [TrackSelectionParameters] instance with audio track enabled.
 *
 * @return A new [TrackSelectionParameters] instance with audio track enabled.
 */
fun TrackSelectionParameters.enableAudioTrack(): TrackSelectionParameters {
    return enableTrackType(C.TRACK_TYPE_AUDIO)
}

/**
 * Creates a new [TrackSelectionParameters] instance with video track enabled.
 *
 * @return A new [TrackSelectionParameters] instance with video track enabled.
 */
fun TrackSelectionParameters.enableVideoTrack(): TrackSelectionParameters {
    return enableTrackType(C.TRACK_TYPE_VIDEO)
}

/**
 * Creates a new [TrackSelectionParameters] instance with text track disabled.
 *
 * @return A new [TrackSelectionParameters] instance with text track disabled.
 */
fun TrackSelectionParameters.disableTextTrack(): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
        .setPreferredTextRoleFlags(0)
        .setPreferredTextLanguage(null)
        .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_FORCED.inv())
        .build()
}

/**
 * Creates a new [TrackSelectionParameters] instance with audio track disabled.
 *
 * @return A new [TrackSelectionParameters] instance with audio track disabled.
 */
fun TrackSelectionParameters.disableAudioTrack(): TrackSelectionParameters {
    return disableTrackType(C.TRACK_TYPE_AUDIO)
}

/**
 * Creates a new [TrackSelectionParameters] instance with video track disabled.
 *
 * @return A new [TrackSelectionParameters] instance with video track disabled.
 */
fun TrackSelectionParameters.disableVideoTrack(): TrackSelectionParameters {
    return disableTrackType(C.TRACK_TYPE_VIDEO)
}

/**
 * Returns a copy of this [TrackSelectionParameters] with default settings for text tracks.
 *
 * @return A new [TrackSelectionParameters] instance with default settings for text tracks.
 */
fun TrackSelectionParameters.defaultTextTrack(): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
        .setIgnoredTextSelectionFlags(0)
        .setPreferredTextLanguage(null)
        .setPreferredTextRoleFlags(0)
        .setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings()
        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
        .build()
}

/**
 * Returns a copy of this [TrackSelectionParameters] with default settings for audio tracks.
 *
 * @param context The [Context].
 * @return A new [TrackSelectionParameters] instance with default settings for audio tracks.
 */
fun TrackSelectionParameters.defaultAudioTrack(context: Context? = null): TrackSelectionParameters {
    return buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
        .setPreferredAudioLanguage(null)
        .setPreferredAudioMimeType(null)
        .setPreferredAudioRoleFlags(0)
        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
        .apply {
            context?.let {
                setPreferredAudioRoleFlagsToAccessibilityManagerSettings(it)
            }
        }
        .build()
}

/**
 * Returns a copy of this [TrackSelectionParameters] with default settings for video tracks.
 *
 * @return A new [TrackSelectionParameters] instance with default settings for video tracks.
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
 * Applies a [TrackSelectionOverride] to the current [TrackSelectionParameters].
 *
 * **Track Override Behavior:**
 *
 * - **Audio:** sets the preferred audio language, which is crucial for handling forced subtitles correctly.
 * - **Text:** sets the preferred text language and role flags based on the override.
 * - **Video:** sets the maximum video size.
 *
 * @param override The [TrackSelectionOverride] containing the track selection criteria.
 * @return A new [TrackSelectionParameters] instance with the override applied.
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
 * Sets preferred audio role flags based on [AccessibilityManager] settings.
 *
 * This function does nothing on Android below Tiramisu or if the user has not enabled audio description in the accessibility settings.
 *
 * @param context The [Context] used to access the [AccessibilityManager].
 * @return This [TrackSelectionParameters.Builder] for method chaining.
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
