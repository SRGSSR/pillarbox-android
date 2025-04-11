/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import com.google.android.gms.cast.MediaTrack

/**
 * Cast track selector
 */
interface CastTrackSelector {

    /**
     * Returns the indices of the currently selected media tracks.
     *
     * This function determines the active tracks based on the provided [parameters]
     * and the available [tracks]. It returns an array containing the indices of these tracks.
     *
     * @param parameters The track selection preferences.
     * @param tracks The available media tracks.
     * @return An array of track indices for the selected tracks. Returns an empty array if no tracks are selected.
     * @see TrackSelectionParameters
     * @see Tracks
     */
    fun getActiveMediaTracks(parameters: TrackSelectionParameters, tracks: List<MediaTrack>): LongArray
}
