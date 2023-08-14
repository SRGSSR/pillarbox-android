/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.extension.filterUnsupported
import org.junit.Assert
import org.junit.Test

class TestUnsupportedTracks {

    @Test
    fun test1() {
        val format1 = createSampleFormat("F1")
        val format2 = createSampleFormat("F2")
        val formatUnsupportedTrack = createSampleFormat("Unsupported")
        val listFormat = listOf(format1, format2, formatUnsupportedTrack)
        val trackGroup = TrackGroup(*listFormat.toTypedArray())
        val selected = BooleanArray(listFormat.size)
        val trackSupport = IntArray(listFormat.size)
        trackSupport[0] = C.FORMAT_HANDLED
        trackSupport[1] = C.FORMAT_HANDLED
        trackSupport[2] = C.FORMAT_UNSUPPORTED_SUBTYPE
        val tracks = Tracks(listOf(Tracks.Group(trackGroup, false, trackSupport, selected)))

        val expectedTracksGroups = listOf(
            Tracks.Group(
                TrackGroup(*listOf(format1, format2).toTypedArray()), false, IntArray(2){
                          C.FORMAT_HANDLED
                }, BooleanArray
                    (2)
            )
        )
        Assert.assertEquals(expectedTracksGroups, tracks.groups.mapNotNull { it.filterUnsupported() })
    }

    companion object {
        private fun createSampleFormat(
            label: String
        ):
            Format {
            return Format.Builder()
                .setId("id:${label}")
                .setLabel(label)
                .setContainerMimeType(MimeTypes.AUDIO_MP4)
                .build()
        }
    }

}
