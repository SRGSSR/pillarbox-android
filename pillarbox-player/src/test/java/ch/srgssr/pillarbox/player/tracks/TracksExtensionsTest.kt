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
import ch.srgssr.pillarbox.player.extension.isForced
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class TracksExtensionsTest {
    private val tracks = Tracks(
        listOf(
            // Video tracks
            Tracks.Group(
                TrackGroup(
                    Format.Builder()
                        .setId("1_V_video_1")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(2001694)
                        .setCodecs("avc1.4D401F")
                        .setWidth(960)
                        .setHeight(540)
                        .build(),
                    Format.Builder()
                        .setId("1_V_video_2")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(500390)
                        .setCodecs("avc1.4D401F")
                        .setWidth(484)
                        .setHeight(272)
                        .build(),
                    Format.Builder()
                        .setId("1_V_video_3")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(6003201)
                        .setCodecs("avc1.640029")
                        .setWidth(1920)
                        .setHeight(1080)
                        .build(),
                    Format.Builder()
                        .setId("1_V_video_4")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(1201016)
                        .setCodecs("avc1.4D401F")
                        .setWidth(640)
                        .setHeight(360)
                        .build(),
                    Format.Builder()
                        .setId("1_V_video_5")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(3502299)
                        .setCodecs("avc1.4D401F")
                        .setWidth(1280)
                        .setHeight(720)
                        .build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(true, true, true, true, true),
            ),
            // Audio tracks (fr)
            Tracks.Group(
                TrackGroup(
                    Format.Builder()
                        .setId("5_A_audio_fra_1")
                        .setSampleMimeType(MimeTypes.AUDIO_AAC)
                        .setContainerMimeType(MimeTypes.AUDIO_MP4)
                        .setPeakBitrate(128000)
                        .setCodecs("mp4a.40.2")
                        .setSampleRate(48000)
                        .setLanguage("fr")
                        .build(),
                    Format.Builder()
                        .setId("5_A_audio_fra_2")
                        .setSampleMimeType(MimeTypes.AUDIO_AAC)
                        .setContainerMimeType(MimeTypes.AUDIO_MP4)
                        .setPeakBitrate(128000)
                        .setCodecs("mp4a.40.2")
                        .setSampleRate(48000)
                        .setLanguage("fr")
                        .build(),
                    Format.Builder()
                        .setId("5_A_audio_fra_3")
                        .setSampleMimeType(MimeTypes.AUDIO_AAC)
                        .setContainerMimeType(MimeTypes.AUDIO_MP4)
                        .setPeakBitrate(128000)
                        .setCodecs("mp4a.40.2")
                        .setSampleRate(48000)
                        .setLanguage("fr")
                        .build(),
                    Format.Builder()
                        .setId("5_A_audio_fra_4")
                        .setSampleMimeType(MimeTypes.AUDIO_AAC)
                        .setContainerMimeType(MimeTypes.AUDIO_MP4)
                        .setPeakBitrate(128000)
                        .setCodecs("mp4a.40.2")
                        .setSampleRate(48000)
                        .setLanguage("fr")
                        .build(),
                    Format.Builder()
                        .setId("5_A_audio_fra_5")
                        .setSampleMimeType(MimeTypes.AUDIO_AAC)
                        .setContainerMimeType(MimeTypes.AUDIO_MP4)
                        .setPeakBitrate(128000)
                        .setCodecs("mp4a.40.2")
                        .setSampleRate(48000)
                        .setLanguage("fr")
                        .build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(false, false, false, false, false),
            ),
            // Audio tracks (en)
            Tracks.Group(
                TrackGroup(
                    Format.Builder()
                        .setId("5_A_audio_eng_1")
                        .setSampleMimeType(MimeTypes.AUDIO_AAC)
                        .setContainerMimeType(MimeTypes.AUDIO_MP4)
                        .setPeakBitrate(128000)
                        .setCodecs("mp4a.40.2")
                        .setSampleRate(48000)
                        .setLanguage("en")
                        .build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED),
                booleanArrayOf(true),
            ),
            // Text tracks
            Tracks.Group(
                TrackGroup(
                    Format.Builder()
                        .setId("fr")
                        .setSampleMimeType(MimeTypes.TEXT_VTT)
                        .setContainerMimeType(MimeTypes.TEXT_VTT)
                        .setPeakBitrate(0)
                        .setLanguage("text-fra-sdh")
                        .build(),
                    Format.Builder()
                        .setId("en")
                        .setSampleMimeType(MimeTypes.TEXT_VTT)
                        .setContainerMimeType(MimeTypes.TEXT_VTT)
                        .setPeakBitrate(0)
                        .setLanguage("text-eng-sdh")
                        .setSelectionFlags(C.SELECTION_FLAG_FORCED)
                        .build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(false, true),
            ),
        )
    )

    @Test
    fun `get tracks`() {
        val tracks = this.tracks.tracks

        assertEquals(8, tracks.size)
        assertTrue(tracks.all { it.isSupported })
    }

    @Test
    fun `get audio tracks`() {
        val tracks = this.tracks.audioTracks

        assertEquals(2, tracks.size)
        assertTrue(tracks.all { it.isSupported })
    }

    @Test
    fun `get text tracks`() {
        val tracks = this.tracks.textTracks

        assertEquals(1, tracks.size)
        assertTrue(tracks.all { it.isSupported })
        assertTrue(tracks.none { it.format.isForced() })
    }

    @Test
    fun `get video tracks`() {
        val tracks = this.tracks.videoTracks

        assertEquals(5, tracks.size)
        assertTrue(tracks.all { it.isSupported })
    }
}
