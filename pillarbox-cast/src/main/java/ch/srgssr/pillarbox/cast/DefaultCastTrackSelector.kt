/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks

/**
 * Default cast track selector
 * Support only [TrackSelectionOverride] from [TrackSelectionParameters.overrides].
 */
class DefaultCastTrackSelector : CastTrackSelector {

    override fun getActiveMediaTracks(
        parameters: TrackSelectionParameters,
        tracks: Tracks
    ): LongArray {
        val trackIdToSelect = mutableListOf<Long>()
        for (trackGroup in parameters.overrides.keys) {
            runCatching {
                trackIdToSelect.add(trackGroup.id.toLong())
            }
        }
        return trackIdToSelect.toLongArray()
    }
}
