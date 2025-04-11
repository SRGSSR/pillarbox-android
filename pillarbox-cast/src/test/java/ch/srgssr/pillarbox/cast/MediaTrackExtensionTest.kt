/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import ch.srgssr.pillarbox.cast.extension.CAST_TEXT_TRACK
import ch.srgssr.pillarbox.cast.extension.toFormat
import com.google.android.gms.cast.MediaTrack
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class MediaTrackExtensionTest(
    val mediaTrack: MediaTrack,
    val format: Format
) {

    @Test
    fun `MediaTrack to Format`() {
        assertEquals(format, mediaTrack.toFormat())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Iterable<Any> {
            return listOf(
                arrayOf(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setLanguage("en")
                        .setContentType("unknown")
                        .setContentId("ID")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setSampleMimeType(CAST_TEXT_TRACK)
                        .setContainerMimeType("unknown")
                        .setLanguage("en")
                        .build()
                ),
                arrayOf(
                    MediaTrack.Builder(0, MediaTrack.TYPE_AUDIO)
                        .setLanguage("en")
                        .setContentId("ID")
                        .setContentType("unknown")
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setLanguage("en")
                        .setContainerMimeType("unknown")
                        .build()
                ),
                arrayOf(
                    MediaTrack.Builder(0, MediaTrack.TYPE_AUDIO)
                        .setLanguage("en")
                        .setContentId("ID")
                        .setContentType(MimeTypes.AUDIO_AAC)
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setLanguage("en")
                        .setContainerMimeType(MimeTypes.AUDIO_AAC)
                        .build()
                ),
                arrayOf(
                    MediaTrack.Builder(0, MediaTrack.TYPE_VIDEO)
                        .setContentId("ID")
                        .setContentType(MimeTypes.VIDEO_H263)
                        .build(),
                    Format.Builder()
                        .setId("ID")
                        .setContainerMimeType(MimeTypes.VIDEO_H263)
                        .build()
                ),
            )
        }
    }
}
