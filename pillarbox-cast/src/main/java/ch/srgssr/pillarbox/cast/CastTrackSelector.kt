/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks

interface CastTrackSelector {
    fun getActiveMediaTracks(parameters: TrackSelectionParameters, tracks: Tracks): LongArray
}
