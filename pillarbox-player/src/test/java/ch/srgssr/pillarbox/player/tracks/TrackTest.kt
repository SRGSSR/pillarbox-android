/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class TrackTest {
    private val tracks = Tracks(
        listOf(
            // Audio tracks
            Tracks.Group(
                TrackGroup(
                    Format.Builder().setSampleMimeType(MimeTypes.AUDIO_MP4).build(),
                    Format.Builder().setSampleMimeType(MimeTypes.AUDIO_AAC).build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(false, true),
            ),
            // Text tracks
            Tracks.Group(
                TrackGroup(
                    Format.Builder().setSampleMimeType(MimeTypes.TEXT_VTT).build(),
                    Format.Builder().setSampleMimeType(MimeTypes.TEXT_SSA).build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(false, true),
            ),
            // Video tracks
            Tracks.Group(
                TrackGroup(
                    Format.Builder().setSampleMimeType(MimeTypes.VIDEO_H265).build(),
                    Format.Builder().setSampleMimeType(MimeTypes.VIDEO_AVI).build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(false, true),
            ),
            // Unsupported track type
            Tracks.Group(
                TrackGroup(
                    Format.Builder().setSampleMimeType(MimeTypes.IMAGE_WEBP).build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED),
                booleanArrayOf(true),
            ),
        )
    )

    @Test
    fun `create audio track`() {
        val groupIndex = 0
        val trackIndexInGroup = 1
        val track = Track(
            group = tracks.groups[groupIndex],
            groupIndex = groupIndex,
            trackIndexInGroup = trackIndexInGroup,
        )

        assertTrue(track is AudioTrack)
        assertEquals(tracks.groups[groupIndex].getTrackFormat(trackIndexInGroup), track.format)
        assertEquals(groupIndex, track.groupIndex)
        assertEquals(trackIndexInGroup, track.trackIndexInGroup)
        assertTrue(track.isSupported)
    }

    @Test
    fun `create text track`() {
        val groupIndex = 1
        val trackIndexInGroup = 1
        val track = Track(
            group = tracks.groups[groupIndex],
            groupIndex = groupIndex,
            trackIndexInGroup = trackIndexInGroup,
        )

        assertTrue(track is TextTrack)
        assertEquals(tracks.groups[groupIndex].getTrackFormat(trackIndexInGroup), track.format)
        assertEquals(groupIndex, track.groupIndex)
        assertEquals(trackIndexInGroup, track.trackIndexInGroup)
        assertTrue(track.isSupported)
    }

    @Test
    fun `create video track`() {
        val groupIndex = 2
        val trackIndexInGroup = 1
        val track = Track(
            group = tracks.groups[groupIndex],
            groupIndex = groupIndex,
            trackIndexInGroup = trackIndexInGroup,
        )

        assertTrue(track is VideoTrack)
        assertEquals(tracks.groups[groupIndex].getTrackFormat(trackIndexInGroup), track.format)
        assertEquals(groupIndex, track.groupIndex)
        assertEquals(trackIndexInGroup, track.trackIndexInGroup)
        assertTrue(track.isSupported)
    }

    @Test(expected = IllegalStateException::class)
    fun `create image track`() {
        val groupIndex = 3
        val trackIndexInGroup = 1

        Track(
            group = tracks.groups[groupIndex],
            groupIndex = groupIndex,
            trackIndexInGroup = trackIndexInGroup,
        )
    }
}
