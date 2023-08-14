/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.C.RoleFlags
import androidx.media3.common.C.SelectionFlags
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.extension.audio
import ch.srgssr.pillarbox.player.extension.text
import ch.srgssr.pillarbox.player.extension.video
import org.junit.Assert
import org.junit.Test

class TestTracksExtension {
    private val listTextTracks = listOf(
        createTrackGroup(
            listOf(
                createFormatTrackFormat("t1", mimeType = TEXT_MIME_TYPE),
                createFormatTrackFormat("t2", mimeType = TEXT_MIME_TYPE, selectionFlags = C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT),
            ),
        ),
        createTrackGroup(
            listOf(
                createFormatTrackFormat("t1-sdh", mimeType = TEXT_MIME_TYPE, roleFlags = C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND),
            ),
        ),
    )
    private val listForcedSubtitles = listOf(
        createTrackGroup(
            listOf(
                createFormatTrackFormat("t1-forced", mimeType = TEXT_MIME_TYPE, selectionFlags = C.SELECTION_FLAG_FORCED),
            ),
        ),
    )
    private val listAudios = listOf(
        createTrackGroup(
            listOf(
                createFormatTrackFormat("a1", mimeType = AUDIO_MIME_TYPE),
            ),
        ),
        createTrackGroup(
            listOf(
                createFormatTrackFormat("a1-ad", mimeType = AUDIO_MIME_TYPE, roleFlags = C.ROLE_FLAG_DESCRIBES_VIDEO),
            ),
        ),
    )

    private val listVideos = listOf(
        createTrackGroup(
            listOf(
                createFormatTrackFormat("v1", mimeType = VIDEO_MIME_TYPE),
                createFormatTrackFormat("v2", mimeType = VIDEO_MIME_TYPE),
                createFormatTrackFormat("v3", mimeType = VIDEO_MIME_TYPE),
            ),
        ),
    )
    private val tracks = Tracks(listAudios + listTextTracks + listVideos + listForcedSubtitles)

    @Test
    fun testTextTracks() {
        val textTracks = tracks.text
        Assert.assertEquals(listTextTracks, textTracks)
    }

    @Test
    fun testAudioTracks() {
        val audioTracks = tracks.audio
        Assert.assertEquals(listAudios, audioTracks)
    }

    @Test
    fun testVideoTracks() {
        val videoTracks = tracks.video
        Assert.assertEquals(listVideos, videoTracks)
    }

    companion object {

        private const val TEXT_MIME_TYPE = MimeTypes.APPLICATION_TTML
        private const val VIDEO_MIME_TYPE = MimeTypes.VIDEO_H265
        private const val AUDIO_MIME_TYPE = MimeTypes.AUDIO_MP4

        fun createFormatTrackFormat(
            label: String? = null,
            mimeType: String,
            roleFlags: @RoleFlags Int = 0,
            selectionFlags: @SelectionFlags Int = C.SELECTION_FLAG_AUTOSELECT
        ):
            Format {
            return Format.Builder()
                .setId("id:${label}")
                .setLabel(label)
                .setLanguage("fr")
                .setSelectionFlags(selectionFlags)
                .setRoleFlags(roleFlags)
                .setContainerMimeType(mimeType)
                .build()
        }

        fun createTrackGroup(listFormat: List<Format>): Tracks.Group {
            val trackGroup = TrackGroup(*listFormat.toTypedArray())
            val trackSupport = IntArray(listFormat.size){
                C.FORMAT_HANDLED
            }
            val selected = BooleanArray(listFormat.size)
            return Tracks.Group(trackGroup, false, trackSupport, selected)
        }
    }
}
