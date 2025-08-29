/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import com.google.android.gms.cast.MediaTrack
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class DefaultFormatConverterToFormatTest(
    val name: String,
    val mediaTrack: MediaTrack,
    val format: Format
) {
    private val formatAdapter = DefaultFormatConverter()

    @Test
    fun testToFormat() {
        val actualFormat = formatAdapter.toFormat(mediaTrack)
        assertEquals(format, actualFormat, name)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index} - {0}")
        fun parameters(): Iterable<Any> {
            return listOf(
                arrayOf(
                    "Unknown media track with minimal data",
                    MediaTrack.Builder(0, MediaTrack.TYPE_UNKNOWN)
                        .build(),
                    Format.Builder()
                        .build()
                ),
                arrayOf(
                    "Unknown media track with some data and roles",
                    MediaTrack.Builder(0, MediaTrack.TYPE_UNKNOWN)
                        .setLanguage("en")
                        .setContentId("id")
                        .setRoles(listOf(MediaTrack.ROLE_MAIN))
                        .build(),
                    Format.Builder()
                        .setLanguage("en")
                        .setId("id")
                        .build()
                ),
                arrayOf(
                    "webvtt text track without roles",
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setLanguage("en")
                        .setContentType("mp4/webvtt")
                        .setContentId("ID")
                        .setName("TextTrack1")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setSampleMimeType(MimeTypes.TEXT_UNKNOWN)
                        .setLanguage("en")
                        .setLabel("TextTrack1")
                        .build()
                ),
                arrayOf(
                    "ttml text track without roles",
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setLanguage("en")
                        .setContentType(MimeTypes.APPLICATION_TTML)
                        .setContentId("ID")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setSampleMimeType(MimeTypes.APPLICATION_TTML)
                        .setLanguage("en")
                        .build()
                ),
                arrayOf(
                    "unknown audio track without roles",
                    MediaTrack.Builder(0, MediaTrack.TYPE_AUDIO)
                        .setLanguage("en")
                        .setContentId("ID")
                        .setContentType("unknown")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setLanguage("en")
                        .setSampleMimeType(MimeTypes.AUDIO_UNKNOWN)
                        .build()
                ),
                arrayOf(
                    "aac audio track without roles",
                    MediaTrack.Builder(0, MediaTrack.TYPE_AUDIO)
                        .setLanguage("en")
                        .setContentId("ID")
                        .setContentType(MimeTypes.AUDIO_AAC)
                        .setName("AudioTrack1")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setLanguage("en")
                        .setLabel("AudioTrack1")
                        .setSampleMimeType(MimeTypes.AUDIO_AAC)
                        .build()
                ),
                arrayOf(
                    "unknown video track without roles",
                    MediaTrack.Builder(0, MediaTrack.TYPE_VIDEO)
                        .setContentId("ID")
                        .setContentType("unknown")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setSampleMimeType(MimeTypes.VIDEO_UNKNOWN)
                        .build()
                ),
                arrayOf(
                    "h263 video track without roles",
                    MediaTrack.Builder(0, MediaTrack.TYPE_VIDEO)
                        .setContentId("ID")
                        .setContentType(MimeTypes.VIDEO_H263)
                        .setName("VideoTrack1")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setSampleMimeType(MimeTypes.VIDEO_H263)
                        .setLabel("VideoTrack1")
                        .build()
                ),
            )
        }
    }
}
