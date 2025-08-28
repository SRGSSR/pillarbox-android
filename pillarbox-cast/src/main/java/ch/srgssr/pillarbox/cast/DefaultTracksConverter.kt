/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.cast.TracksConverter.CastTracksInfo
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.VideoTrack
import ch.srgssr.pillarbox.player.tracks.tracks
import com.google.android.gms.cast.MediaTrack

/**
 * A default implementation of [TracksConverter].
 */
class DefaultTracksConverter(private val formatConverter: FormatConverter = DefaultFormatConverter()) : TracksConverter {

    override fun toTracks(
        listMediaTracks: List<MediaTrack>,
        activeTrackIds: LongArray
    ): Tracks {
        return if (listMediaTracks.isEmpty()) {
            Tracks.EMPTY
        } else {
            val tabTrackGroup = listMediaTracks.map { mediaTrack ->
                val trackGroup = TrackGroup(mediaTrack.id.toString(), formatConverter.toFormat(mediaTrack))
                Tracks.Group(trackGroup, false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(activeTrackIds.contains(mediaTrack.id)))
            }
            return Tracks(tabTrackGroup)
        }
    }

    override fun toCastTracksInfo(tracks: Tracks): CastTracksInfo {
        val pillarboxTracks = tracks.tracks
        if (pillarboxTracks.isEmpty()) return CastTracksInfo(emptyList(), longArrayOf(), emptyList())
        val listMediaTracks = mutableListOf<MediaTrack>()
        val activeTrackIds = mutableListOf<Long>()
        val trackSelectionOverrides = mutableListOf<TrackSelectionOverride>()
        pillarboxTracks.forEachIndexed { index, track ->
            val trackType = when (track) {
                is TextTrack -> C.TRACK_TYPE_TEXT
                is AudioTrack -> C.TRACK_TYPE_AUDIO
                is VideoTrack -> C.TRACK_TYPE_VIDEO
            }
            val id = index.toLong()
            listMediaTracks.add(formatConverter.toMediaTrack(trackType, id, track.format))
            if (track.isSelected) activeTrackIds.add(id)
            trackSelectionOverrides.add(track.trackSelectionOverride)
        }
        return CastTracksInfo(listMediaTracks, activeTrackIds.toLongArray(), trackSelectionOverrides)
    }
}
