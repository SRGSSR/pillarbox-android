/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.C.RoleFlags
import androidx.media3.common.C.SelectionFlags
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class TracksTest {
    private val textTracks = listOf(
        createTrackGroup(
            createFormatTrackFormat("t1", mimeType = TEXT_MIME_TYPE),
            createFormatTrackFormat("t2", mimeType = TEXT_MIME_TYPE, selectionFlags = C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT),
        ),
        createTrackGroup(
            formats = listOf(
                createFormatTrackFormat("t1-sdh", mimeType = TEXT_MIME_TYPE, roleFlags = C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND),
                createFormatTrackFormat("t3", mimeType = TEXT_MIME_TYPE),
            ),
            trackSupport = intArrayOf(
                C.FORMAT_HANDLED, C.FORMAT_EXCEEDS_CAPABILITIES,
            ),
        ),
    )
    private val forcedSubtitleTracks = listOf(
        createTrackGroup(
            createFormatTrackFormat("t1-forced", mimeType = TEXT_MIME_TYPE, selectionFlags = C.SELECTION_FLAG_FORCED),
        ),
    )
    private val audioTracks = listOf(
        createTrackGroup(
            createFormatTrackFormat("a1", mimeType = AUDIO_MIME_TYPE),
        ),
        createTrackGroup(
            formats = listOf(
                createFormatTrackFormat("a1-ad", mimeType = AUDIO_MIME_TYPE, roleFlags = C.ROLE_FLAG_DESCRIBES_VIDEO),
                createFormatTrackFormat("a2", mimeType = AUDIO_MIME_TYPE),
            ),
            trackSupport = intArrayOf(
                C.FORMAT_HANDLED, C.FORMAT_EXCEEDS_CAPABILITIES
            ),
        ),
    )
    private val videoTracks = listOf(
        createTrackGroup(
            formats = listOf(
                createFormatTrackFormat("v1", mimeType = VIDEO_MIME_TYPE),
                createFormatTrackFormat("v2", mimeType = VIDEO_MIME_TYPE),
                createFormatTrackFormat("v3", mimeType = VIDEO_MIME_TYPE),
                createFormatTrackFormat("v4", mimeType = VIDEO_MIME_TYPE),
            ),
            trackSupport = intArrayOf(
                C.FORMAT_HANDLED, C.FORMAT_HANDLED, C.FORMAT_EXCEEDS_CAPABILITIES, C.FORMAT_HANDLED,
            ),
        ),
    )
    private val tracks = Tracks(audioTracks + textTracks + videoTracks + forcedSubtitleTracks)

    @Test
    fun `text with text tracks`() {
        val textTracks = tracks.text
        assertEquals(this.textTracks + forcedSubtitleTracks, textTracks)
    }

    @Test
    fun `text with empty tracks`() {
        val textTracks = Tracks(emptyList()).text
        assertEquals(emptyList(), textTracks)
    }

    @Test
    fun `audio with audio tracks`() {
        val audioTracks = tracks.audio
        assertEquals(this.audioTracks, audioTracks)
    }

    @Test
    fun `audio with empty tracks`() {
        val audioTracks = Tracks(emptyList()).audio
        assertEquals(emptyList(), audioTracks)
    }

    @Test
    fun `video with video tracks`() {
        val videoTracks = tracks.video
        assertEquals(this.videoTracks, videoTracks)
    }

    @Test
    fun `video with empty tracks`() {
        val videoTracks = Tracks(emptyList()).video
        assertEquals(emptyList(), videoTracks)
    }

    private companion object {
        private const val TEXT_MIME_TYPE = MimeTypes.APPLICATION_TTML
        private const val VIDEO_MIME_TYPE = MimeTypes.VIDEO_H265
        private const val AUDIO_MIME_TYPE = MimeTypes.AUDIO_MP4

        private fun createFormatTrackFormat(
            label: String,
            mimeType: String,
            roleFlags: @RoleFlags Int = 0,
            selectionFlags: @SelectionFlags Int = C.SELECTION_FLAG_AUTOSELECT
        ): Format {
            return Format.Builder()
                .setId("id:$label")
                .setLabel(label)
                .setLanguage("fr")
                .setSelectionFlags(selectionFlags)
                .setRoleFlags(roleFlags)
                .setContainerMimeType(mimeType)
                .build()
        }

        private fun createTrackGroup(vararg formats: Format): Tracks.Group {
            val trackSupport = IntArray(formats.size) {
                C.FORMAT_HANDLED
            }
            return createTrackGroup(formats.toList(), trackSupport)
        }

        private fun createTrackGroup(
            formats: List<Format>,
            trackSupport: IntArray,
        ): Tracks.Group {
            val trackGroup = TrackGroup(*formats.toTypedArray())
            val selected = BooleanArray(formats.size)
            return Tracks.Group(trackGroup, false, trackSupport, selected)
        }
    }
}
