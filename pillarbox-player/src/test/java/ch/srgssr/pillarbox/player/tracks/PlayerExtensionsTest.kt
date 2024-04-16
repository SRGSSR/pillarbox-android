/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import io.mockk.every
import io.mockk.mockk
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PlayerExtensionsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
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
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_UNSUPPORTED_TYPE, C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(true, false, true, true, true),
            ),
            // Audio tracks
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
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED),
                booleanArrayOf(false),
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

    private lateinit var player: PillarboxExoPlayer

    @BeforeTest
    fun setUp() {
        player = mockk()
    }

    @Test
    fun `video qualities, no tracks, no preferred video size`() {
        every { player.currentTracks } returns Tracks.EMPTY
        every { player.trackSelectionParameters } returns TrackSelectionParameters.getDefaults(context)

        val videoQualities = player.videoQualities

        assertTrue(videoQualities.isEmpty())
    }

    @Test
    fun `video qualities, no tracks, with preferred video size`() {
        every { player.currentTracks } returns Tracks.EMPTY
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setMaxVideoSize(1280, 720)
            .build()

        val videoQualities = player.videoQualities

        assertTrue(videoQualities.isEmpty())
    }

    @Test
    fun `video qualities, with tracks, no preferred video size`() {
        every { player.currentTracks } returns tracks
        every { player.trackSelectionParameters } returns TrackSelectionParameters.getDefaults(context)

        val videoQualities = player.videoQualities

        assertEquals(4, videoQualities.size)
        assertEquals(listOf(1920, 1280, 960, 640), videoQualities.map { it.format.width })
        assertEquals(listOf(1080, 720, 540, 360), videoQualities.map { it.format.height })
        assertEquals(listOf(true, false, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with tracks, with matched preferred video size`() {
        every { player.currentTracks } returns tracks
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setMaxVideoSize(1280, 720)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(4, videoQualities.size)
        assertEquals(listOf(1920, 1280, 960, 640), videoQualities.map { it.format.width })
        assertEquals(listOf(1080, 720, 540, 360), videoQualities.map { it.format.height })
        assertEquals(listOf(false, true, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with tracks, with unmatched preferred video size`() {
        every { player.currentTracks } returns tracks
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setMaxVideoSize(1024, 600)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(4, videoQualities.size)
        assertEquals(listOf(1920, 1280, 960, 640), videoQualities.map { it.format.width })
        assertEquals(listOf(1080, 720, 540, 360), videoQualities.map { it.format.height })
        assertEquals(listOf(false, false, true, false), videoQualities.map { it.isSelected })
    }
}
