/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.C.FormatSupport
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.extension.filterUnsupported
import org.junit.Assert
import org.junit.Test

class TestUnsupportedTracks {

    @Test
    fun testOneSupportedTrack() {
        val format = createSampleFormat("Unsupported")
        val listFormat = listOf(format)
        val trackGroup = TrackGroup(*listFormat.toTypedArray())
        val selected = BooleanArray(listFormat.size)
        val trackSupport = createAllSupportArray(listFormat.size, C.FORMAT_HANDLED)
        val tracks = Tracks(listOf(Tracks.Group(trackGroup, false, trackSupport, selected)))
        val expectedSize = 1
        val expectedTracksGroups = listOf(
            Tracks.Group(
                TrackGroup(*listOf(format).toTypedArray()),
                false,
                createAllSupportArray(expectedSize, C.FORMAT_HANDLED),
                BooleanArray(expectedSize)
            )
        )
        Assert.assertEquals(expectedTracksGroups, tracks.groups.mapNotNull { it.filterUnsupported() })
    }

    @Test
    fun testOnlySupportedTracks() {
        val format1 = createSampleFormat("F1")
        val format2 = createSampleFormat("F2")
        val listFormat = listOf(format1, format2)
        val trackGroup = TrackGroup(*listFormat.toTypedArray())
        val selected = BooleanArray(listFormat.size)
        val trackSupport = createAllSupportArray(listFormat.size, C.FORMAT_HANDLED)
        val tracks = Tracks(listOf(Tracks.Group(trackGroup, false, trackSupport, selected)))
        val expectedSize = 2
        val expectedTracksGroups = listOf(
            Tracks.Group(
                TrackGroup(*listOf(format1, format2).toTypedArray()),
                false,
                createAllSupportArray(expectedSize, C.FORMAT_HANDLED),
                BooleanArray(expectedSize)
            )
        )
        Assert.assertEquals(expectedTracksGroups, tracks.groups.mapNotNull { it.filterUnsupported() })
    }

    @Test
    fun testMultiTrackOneUnsupported() {
        val format1 = createSampleFormat("F1")
        val format2 = createSampleFormat("F2")
        val formatUnsupportedTrack = createSampleFormat("Unsupported")
        val listFormat = listOf(format1, format2, formatUnsupportedTrack)
        val trackGroup = TrackGroup(*listFormat.toTypedArray())
        val selected = BooleanArray(listFormat.size)
        val trackSupport = createAllSupportArray(listFormat.size, C.FORMAT_HANDLED)
        trackSupport[2] = C.FORMAT_UNSUPPORTED_SUBTYPE
        val tracks = Tracks(listOf(Tracks.Group(trackGroup, false, trackSupport, selected)))
        val expectedSize = 2
        val expectedTracksGroups = listOf(
            Tracks.Group(
                TrackGroup(*listOf(format1, format2).toTypedArray()),
                false,
                createAllSupportArray(expectedSize, C.FORMAT_HANDLED),
                BooleanArray(expectedSize)
            )
        )
        Assert.assertEquals(expectedTracksGroups, tracks.groups.mapNotNull { it.filterUnsupported() })
    }

    @Test
    fun testMultipleUnsupportedTracks() {
        val format1 = createSampleFormat("F1")
        val format2 = createSampleFormat("F2")
        val formatUnsupportedTrack = createSampleFormat("Unsupported")
        val listFormat = listOf(format1, format2, formatUnsupportedTrack)
        val trackGroup = TrackGroup(*listFormat.toTypedArray())
        val selected = BooleanArray(listFormat.size)
        val trackSupport = createAllSupportArray(listFormat.size, C.FORMAT_UNSUPPORTED_SUBTYPE)
        val tracks = Tracks(listOf(Tracks.Group(trackGroup, false, trackSupport, selected)))
        Assert.assertTrue(tracks.groups.mapNotNull { it.filterUnsupported() }.isEmpty())
    }

    @Test
    fun testOneUnsupportedTrack() {
        val formatUnsupportedTrack = createSampleFormat("Unsupported")
        val listFormat = listOf(formatUnsupportedTrack)
        val trackGroup = TrackGroup(*listFormat.toTypedArray())
        val selected = BooleanArray(listFormat.size)
        val trackSupport = createAllSupportArray(listFormat.size, C.FORMAT_UNSUPPORTED_SUBTYPE)
        val tracks = Tracks(listOf(Tracks.Group(trackGroup, false, trackSupport, selected)))
        Assert.assertTrue(tracks.groups.mapNotNull { it.filterUnsupported() }.isEmpty())
    }

    companion object {

        private fun createSampleFormat(label: String): Format {
            return Format.Builder()
                .setId("id:${label}")
                .setLabel(label)
                .setContainerMimeType(MimeTypes.AUDIO_MP4)
                .build()
        }

        private fun createAllSupportArray(size: Int, support: @FormatSupport Int): IntArray {
            return IntArray(size) {
                support
            }
        }
    }

}
