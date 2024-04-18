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
    private val singleVideoTrackGroup = Tracks(
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
                    // Duplicated format with id=1_V_video_5
                    Format.Builder()
                        .setId("1_V_video_6")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(3502299)
                        .setCodecs("avc1.4D401F")
                        .setWidth(1280)
                        .setHeight(720)
                        .build(),
                    // TrickPlay format
                    Format.Builder()
                        .setId("1_V_video_7")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.APPLICATION_M3U8)
                        .setPeakBitrate(23000)
                        .setCodecs("avc1.42C00D")
                        .setWidth(224)
                        .setHeight(100)
                        .setRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                        .build(),
                    // Track without resolution
                    Format.Builder()
                        .setId("1_V_video_8")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.APPLICATION_M3U8)
                        .setPeakBitrate(23000)
                        .setCodecs("avc1.42C00D")
                        .setRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                        .build(),
                ),
                true,
                IntArray(8) { C.FORMAT_HANDLED },
                BooleanArray(8) { true },
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
    private val doubleVideoTrackGroup = Tracks(
        listOf(
            Tracks.Group(
                TrackGroup(
                    Format.Builder()
                        .setId("video_eng=401000")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(401000)
                        .setCodecs("avc1.42C00D")
                        .setWidth(224)
                        .setHeight(100)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                    Format.Builder()
                        .setId("video_eng=751000")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(751000)
                        .setCodecs("avc1.42C016")
                        .setWidth(448)
                        .setHeight(200)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                    Format.Builder()
                        .setId("video_eng=1001000")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(1001000)
                        .setCodecs("avc1.4D401F")
                        .setWidth(784)
                        .setHeight(350)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                    Format.Builder()
                        .setId("video_eng=1501000")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(1501000)
                        .setCodecs("avc1.640028")
                        .setWidth(1680)
                        .setHeight(750)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                    Format.Builder()
                        .setId("video_eng=2200000")
                        .setSampleMimeType(MimeTypes.VIDEO_H264)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(2200000)
                        .setCodecs("avc1.640028")
                        .setWidth(1680)
                        .setHeight(750)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(true, true, true, true, true),
            ),
            Tracks.Group(
                TrackGroup(
                    Format.Builder()
                        .setId("video_eng_1=902000")
                        .setSampleMimeType(MimeTypes.VIDEO_H265)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(902000)
                        .setCodecs("hvc1.1.6.L150.90")
                        .setWidth(1680)
                        .setHeight(750)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                    Format.Builder()
                        .setId("video_eng_1=1161000")
                        .setSampleMimeType(MimeTypes.VIDEO_H265)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(1161000)
                        .setCodecs("hvc1.1.6.L150.90")
                        .setWidth(2576)
                        .setHeight(1150)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                    Format.Builder()
                        .setId("video_eng_1=1583000")
                        .setSampleMimeType(MimeTypes.VIDEO_H265)
                        .setContainerMimeType(MimeTypes.VIDEO_MP4)
                        .setPeakBitrate(1583000)
                        .setCodecs("hvc1.1.6.L150.90")
                        .setWidth(3360)
                        .setHeight(1500)
                        .setFrameRate(24f)
                        .setLanguage("en")
                        .setRoleFlags(C.ROLE_FLAG_MAIN)
                        .build(),
                ),
                true,
                intArrayOf(C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_HANDLED),
                booleanArrayOf(false, false, false),
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
    fun `video qualities, with tracks, disabled video track`() {
        every { player.currentTracks } returns singleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(5, videoQualities.size)
        assertEquals(listOf(1920, 1280, 960, 640, 484), videoQualities.map { it.width })
        assertEquals(listOf(1080, 720, 540, 360, 272), videoQualities.map { it.height })
        assertEquals(listOf(false, false, false, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with tracks, no preferred video size`() {
        every { player.currentTracks } returns singleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.getDefaults(context)

        val videoQualities = player.videoQualities

        assertEquals(5, videoQualities.size)
        assertEquals(listOf(1920, 1280, 960, 640, 484), videoQualities.map { it.width })
        assertEquals(listOf(1080, 720, 540, 360, 272), videoQualities.map { it.height })
        assertEquals(listOf(true, false, false, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with tracks, with matched preferred video size`() {
        every { player.currentTracks } returns singleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setMaxVideoSize(1280, 720)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(5, videoQualities.size)
        assertEquals(listOf(1920, 1280, 960, 640, 484), videoQualities.map { it.width })
        assertEquals(listOf(1080, 720, 540, 360, 272), videoQualities.map { it.height })
        assertEquals(listOf(false, true, false, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with tracks, with unmatched preferred video size`() {
        every { player.currentTracks } returns singleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setMaxVideoSize(1024, 600)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(5, videoQualities.size)
        assertEquals(listOf(1920, 1280, 960, 640, 484), videoQualities.map { it.width })
        assertEquals(listOf(1080, 720, 540, 360, 272), videoQualities.map { it.height })
        assertEquals(listOf(false, false, true, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with two video groups, disabled video track`() {
        every { player.currentTracks } returns doubleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(6, videoQualities.size)
        assertEquals(listOf(3360, 2576, 1680, 784, 448, 224), videoQualities.map { it.width })
        assertEquals(listOf(1500, 1150, 750, 350, 200, 100), videoQualities.map { it.height })
        assertEquals(listOf(false, false, false, false, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with two video groups, no preferred video size`() {
        every { player.currentTracks } returns doubleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.getDefaults(context)

        val videoQualities = player.videoQualities

        assertEquals(4, videoQualities.size)
        assertEquals(listOf(1680, 784, 448, 224), videoQualities.map { it.width })
        assertEquals(listOf(750, 350, 200, 100), videoQualities.map { it.height })
        assertEquals(listOf(true, false, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with two video groups, with matched preferred video size`() {
        every { player.currentTracks } returns doubleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setMaxVideoSize(2576, 1150)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(4, videoQualities.size)
        assertEquals(listOf(1680, 784, 448, 224), videoQualities.map { it.width })
        assertEquals(listOf(750, 350, 200, 100), videoQualities.map { it.height })
        assertEquals(listOf(true, false, false, false), videoQualities.map { it.isSelected })
    }

    @Test
    fun `video qualities, with two video groups, with unmatched preferred video size`() {
        every { player.currentTracks } returns doubleVideoTrackGroup
        every { player.trackSelectionParameters } returns TrackSelectionParameters.Builder(context)
            .setMaxVideoSize(1024, 600)
            .build()

        val videoQualities = player.videoQualities

        assertEquals(4, videoQualities.size)
        assertEquals(listOf(1680, 784, 448, 224), videoQualities.map { it.width })
        assertEquals(listOf(750, 350, 200, 100), videoQualities.map { it.height })
        assertEquals(listOf(false, true, false, false), videoQualities.map { it.isSelected })
    }
}
