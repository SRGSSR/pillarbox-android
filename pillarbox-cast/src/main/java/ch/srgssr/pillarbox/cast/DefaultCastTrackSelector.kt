/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import com.google.android.gms.cast.MediaTrack

/**
 * Default cast track selector
 * Support only [TrackSelectionOverride] from [TrackSelectionParameters.overrides].
 */
object DefaultCastTrackSelector : CastTrackSelector {

    override fun getActiveMediaTracks(
        parameters: TrackSelectionParameters,
        tracks: List<MediaTrack>
    ): LongArray {
        return parameters.overrides.keys
            .mapNotNull { it.id.toLongOrNull() }
            .toLongArray()
    }
}
