/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.VideoTrack
import ch.srgssr.pillarbox.player.tracks.tracks
import com.google.android.gms.cast.MediaTrack
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DefaultTracksConverterTest {
    private val formatAdapter = DefaultFormatConverter()
    private val tracksAdapter = DefaultTracksConverter(formatAdapter)

    @Test
    fun testToTracks() {
        val listMediaTracks = listOf(
            MediaTrack.Builder(0, MediaTrack.TYPE_AUDIO).setLanguage("en").setName("english").build(),
            MediaTrack.Builder(1, MediaTrack.TYPE_AUDIO).setLanguage("fr").setName("français").build(),
            MediaTrack.Builder(2, MediaTrack.TYPE_TEXT).setLanguage("fr").setName("français").build(),
            MediaTrack.Builder(3, MediaTrack.TYPE_VIDEO).build(),
            MediaTrack.Builder(4, MediaTrack.TYPE_UNKNOWN).build()
        )
        val activeTrackIds = longArrayOf(0, 2, 3)
        val expectedTracks = Tracks(
            listOf(
                Tracks.Group(TrackGroup("0", formatAdapter.toFormat(listMediaTracks[0])), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(true)),
                Tracks.Group(TrackGroup("1", formatAdapter.toFormat(listMediaTracks[1])), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(false)),
                Tracks.Group(TrackGroup("2", formatAdapter.toFormat(listMediaTracks[2])), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(true)),
                Tracks.Group(TrackGroup("3", formatAdapter.toFormat(listMediaTracks[3])), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(true)),
                Tracks.Group(TrackGroup("4", formatAdapter.toFormat(listMediaTracks[4])), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(false)),
            ),
        )
        assertEquals(expectedTracks, tracksAdapter.toTracks(listMediaTracks, activeTrackIds))
    }

    @Test
    fun testToCastTracksInfo() {
        val tracks = Tracks(
            listOf(
                Tracks.Group(
                    TrackGroup(
                        "video:0",
                        Format.Builder().setSampleMimeType(MimeTypes.VIDEO_H263).build(),
                        Format.Builder().setSampleMimeType(MimeTypes.VIDEO_H263).build()
                    ),
                    true,
                    intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                    booleanArrayOf(true, true)
                ),
                Tracks.Group(
                    TrackGroup("text:0", Format.Builder().setLanguage("en").setSampleMimeType(MimeTypes.APPLICATION_MEDIA3_CUES).build()),
                    false,
                    intArrayOf(C.FORMAT_HANDLED),
                    booleanArrayOf(false)
                ),
                Tracks.Group(
                    TrackGroup("text:1", Format.Builder().setLanguage("fr").setSampleMimeType(MimeTypes.APPLICATION_MEDIA3_CUES).build()),
                    false,
                    intArrayOf(C.FORMAT_HANDLED),
                    booleanArrayOf(true)
                ),
                Tracks.Group(
                    TrackGroup("audio:0", Format.Builder().setSampleMimeType(MimeTypes.AUDIO_AAC).setLanguage("fr").build()),
                    false,
                    intArrayOf(C.FORMAT_HANDLED),
                    booleanArrayOf(true)
                ),
            ),
        )
        val mediaTracks = tracks.tracks.mapIndexed { index, track ->
            val trackType = when (track) {
                is TextTrack -> C.TRACK_TYPE_TEXT
                is AudioTrack -> C.TRACK_TYPE_AUDIO
                is VideoTrack -> C.TRACK_TYPE_VIDEO
            }
            formatAdapter.toMediaTrack(trackType, index.toLong(), track.format)
        }
        val trackOverrides = tracks.tracks.map {
            it.trackSelectionOverride
        }
        val activesIds = tracks.tracks.mapIndexed { index, track ->
            if (track.isSelected) {
                index.toLong()
            } else {
                -1
            }
        }.filter { it >= 0 }
        val castTracksInfo = tracksAdapter.toCastTracksInfo(tracks)
        assertEquals(mediaTracks, castTracksInfo.mediaTracks)
        assertEquals(trackOverrides, castTracksInfo.trackSelectionOverrides)
        assertContentEquals(activesIds.toLongArray(), castTracksInfo.activeTrackIds, "$activesIds")
    }
}
