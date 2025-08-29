/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import com.google.android.gms.cast.MediaTrack

/**
 * Interface responsible for converting between ExoPlayer's [Tracks] and Cast's [MediaTrack] representations.
 * This is necessary because the Cast framework uses its own data structures for track information.
 */
interface TracksConverter {

    /**
     * Holds data from Media3 [Tracks] to be compatible with Cast SDK.
     *
     * @property mediaTracks The list of [MediaTrack].
     * @property activeTrackIds The active track ids.
     * @property trackSelectionOverrides The track selection override to use on the receiver.
     */
    class CastTracksInfo(
        val mediaTracks: List<MediaTrack>,
        val activeTrackIds: LongArray,
        val trackSelectionOverrides: List<TrackSelectionOverride>
    )

    /**
     * Converts a list of [MediaTrack] and active track IDs to [Tracks].
     *
     * @param mediaTracks The list of [MediaTrack] objects.
     * @param activeTrackIds The array of active track IDs.
     * @return The converted [Tracks] object.
     */
    fun toTracks(mediaTracks: List<MediaTrack>, activeTrackIds: LongArray): Tracks

    /**
     * Convert [Tracks] to [CastTracksInfo].
     *
     * @param tracks The tracks to convert.
     * @return The resulting [CastTracksInfo].
     */
    fun toCastTracksInfo(tracks: Tracks): CastTracksInfo
}
