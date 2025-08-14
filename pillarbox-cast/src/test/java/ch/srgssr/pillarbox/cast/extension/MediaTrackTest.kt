/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.extension

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import com.google.android.gms.cast.MediaTrack
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class MediaTrackTest(
    val mediaTrack: MediaTrack,
    val trackGroup: TrackGroup
) {

    @Test
    fun `MediaTrack to TrackGroup`() {
        assertEquals(trackGroup, mediaTrack.toTrackGroup())
        assertEquals(trackGroup.type, getType(mediaTrack.type))
    }

    companion object {

        private fun getType(castType: Int): Int {
            return when (castType) {
                MediaTrack.TYPE_AUDIO -> C.TRACK_TYPE_AUDIO
                MediaTrack.TYPE_VIDEO -> C.TRACK_TYPE_VIDEO
                MediaTrack.TYPE_TEXT -> C.TRACK_TYPE_TEXT
                else -> C.TRACK_TYPE_UNKNOWN
            }
        }

        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Iterable<Any> {
            return listOf(
                arrayOf(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setLanguage("en")
                        .setContentType("mp4/webvtt")
                        .setContentId("ID")
                        .setName("TextTrack1")
                        .build(),
                    TrackGroup(
                        "0",
                        Format.Builder()
                            .setId("ID")
                            .setSampleMimeType(MimeTypes.TEXT_UNKNOWN)
                            .setLanguage("en")
                            .setLabel("TextTrack1")
                            .build()
                    )
                ),
                arrayOf(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setLanguage("en")
                        .setContentType(MimeTypes.APPLICATION_TTML)
                        .setContentId("ID")
                        .build(),
                    TrackGroup(
                        "0",
                        Format.Builder()
                            .setId("ID")
                            .setSampleMimeType(MimeTypes.APPLICATION_TTML)
                            .setLanguage("en")
                            .build()
                    )
                ),
                arrayOf(
                    MediaTrack.Builder(1, MediaTrack.TYPE_AUDIO)
                        .setLanguage("en")
                        .setContentId("ID")
                        .setContentType("unknown")
                        .build(),
                    TrackGroup(
                        "1",
                        Format.Builder()
                            .setId("ID")
                            .setLanguage("en")
                            .setSampleMimeType(MimeTypes.AUDIO_UNKNOWN)
                            .build()
                    )
                ),
                arrayOf(
                    MediaTrack.Builder(2, MediaTrack.TYPE_AUDIO)
                        .setLanguage("en")
                        .setContentId("ID")
                        .setContentType(MimeTypes.AUDIO_AAC)
                        .setName("AudioTrack1")
                        .build(),
                    TrackGroup(
                        "2",
                        Format.Builder()
                            .setId("ID")
                            .setLanguage("en")
                            .setLabel("AudioTrack1")
                            .setSampleMimeType(MimeTypes.AUDIO_AAC)
                            .build()
                    )
                ),
                arrayOf(
                    MediaTrack.Builder(3, MediaTrack.TYPE_VIDEO)
                        .setContentId("ID")
                        .setContentType(MimeTypes.VIDEO_H263)
                        .setName("VideoTrack1")
                        .build(),
                    TrackGroup(
                        "3",
                        Format.Builder()
                            .setId("ID")
                            .setSampleMimeType(MimeTypes.VIDEO_H263)
                            .setLabel("VideoTrack1")
                            .build()
                    )
                ),
                arrayOf(
                    MediaTrack.Builder(3, MediaTrack.TYPE_VIDEO)
                        .setContentId("ID")
                        .setContentType("unknown")
                        .build(),
                    TrackGroup(
                        "3",
                        Format.Builder()
                            .setId("ID")
                            .setSampleMimeType(MimeTypes.VIDEO_UNKNOWN)
                            .build()
                    )
                ),
            )
        }
    }
}
