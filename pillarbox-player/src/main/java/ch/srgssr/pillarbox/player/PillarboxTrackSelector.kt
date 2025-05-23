/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import ch.srgssr.pillarbox.player.extension.setPreferredAudioRoleFlagsToAccessibilityManagerSettings

/**
 * Provides a pre-configured instance of [TrackSelector] suitable for use within Pillarbox.
 *
 * @param context The [Context] required for initializing the [TrackSelector].
 * @return A [TrackSelector] ready for use within Pillarbox.
 */
@Suppress("FunctionName")
fun PillarboxTrackSelector(context: Context): TrackSelector {
    return DefaultTrackSelector(
        context,
        TrackSelectionParameters.Builder()
            .setPreferredAudioRoleFlagsToAccessibilityManagerSettings(context)
            .build(),
    )
}
