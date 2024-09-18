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
 * Preconfigured [TrackSelector] for Pillarbox.
 *
 * @param context The [Context] needed to create the [TrackSelector].
 */
@Suppress("FunctionName")
fun PillarboxTrackSelector(context: Context): TrackSelector {
    return DefaultTrackSelector(
        context,
        TrackSelectionParameters.Builder(context)
            .setPreferredAudioRoleFlagsToAccessibilityManagerSettings(context)
            .build(),
    )
}
