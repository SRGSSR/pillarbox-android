/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import androidx.media3.common.Format
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
     * @property listMediaTracks The list of [MediaTrack].
     * @property activeTrackIds The active track ids.
     * @property trackSelectionOverrides The track selection override to use on the receiver.
     */
    class CastTracksInfo(
        val listMediaTracks: List<MediaTrack>,
        val activeTrackIds: LongArray,
        val trackSelectionOverrides: List<TrackSelectionOverride>
    )

    /**
     * Converts a list of [MediaTrack] and active track IDs to [Tracks].
     *
     * @param listMediaTracks The list of [MediaTrack] objects.
     * @param activeTrackIds The array of active track IDs.
     * @return The converted [Tracks] object.
     */
    fun toTracks(listMediaTracks: List<MediaTrack>, activeTrackIds: LongArray): Tracks

    /**
     * Convert [Tracks] to [CastTracksInfo].
     *
     * @param tracks The tracks to convert.
     * @return The resulting [CastTracksInfo].
     */
    fun toCastTracksInfo(tracks: Tracks): CastTracksInfo
}

/**
 * Interface responsible for converting between ExoPlayer's [Format] and Cast's [MediaTrack] representations.
 */
interface FormatConverter {

    /**
     * Convert a [Format] to a [MediaTrack] respecting the following specifications https://developers.google.com/cast/docs/reference/web_receiver/cast.framework.messages.Track#roles.
     */
    fun toMediaTrack(trackType: @C.TrackType Int, trackId: Long, format: Format): MediaTrack

    /**
     * Convert a [MediaTrack] to a [Format].
     */
    fun toFormat(mediaTrack: MediaTrack): Format
}
