/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import com.google.android.gms.cast.MediaTrack
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class DefaultFormatConverterToMediaTrackTest(internal val case: TestCase) {
    private val formatAdapter = DefaultFormatConverter()

    @Test
    fun testToMediaTrack() {
        val actualMediaTrack = formatAdapter.toMediaTrack(case.trackType, case.trackId, case.format)
        assertEquals(case.mediaTrack, actualMediaTrack)
    }

    class TestCase(
        val name: String,
        val trackType: @C.TrackType Int,
        val trackId: Long,
        val format: Format,
        val mediaTrack: MediaTrack,
    ) {
        override fun toString(): String {
            return name
        }
    }

    companion object {

        private fun testCase(
            name: String,
            trackType: @C.TrackType Int,
            trackId: Long = 0,
            format: Format.Builder.() -> Unit = {},
            mediaTrack: MediaTrack.Builder.() -> Unit = {},
        ): TestCase {
            val mediaTrackType = when (trackType) {
                C.TRACK_TYPE_TEXT -> MediaTrack.TYPE_TEXT
                C.TRACK_TYPE_AUDIO -> MediaTrack.TYPE_AUDIO
                C.TRACK_TYPE_VIDEO -> MediaTrack.TYPE_VIDEO
                else -> MediaTrack.TYPE_UNKNOWN
            }
            return TestCase(
                name = name,
                trackType = trackType,
                trackId = trackId,
                format = Format.Builder()
                    .setLanguage("en")
                    .setId("StringId")
                    .setLabel("TrackName")
                    .apply(format)
                    .build(),
                mediaTrack = MediaTrack.Builder(trackId, mediaTrackType)
                    .setLanguage("en")
                    .setContentId("StringId")
                    .setName("TrackName")
                    .apply(mediaTrack)
                    .build()
            )
        }

        private fun textRoleTestCase(
            roleFlags: Int,
            name: String = "TextTrackRoleFlags = $roleFlags",
            subType: Int = MediaTrack.SUBTYPE_NONE,
            roles: List<String>? = null,
            selectionFlags: Int = 0
        ) =
            testCase(
                name = name,
                trackType = C.TRACK_TYPE_TEXT,
                format = {
                    setRoleFlags(roleFlags)
                    setSelectionFlags(selectionFlags)
                },
                mediaTrack = {
                    setRoles(roles?.sorted())
                    setSubtype(subType)
                }
            )

        private fun audioRoleTestCase(
            roleFlags: Int,
            name: String = "AudioTrackRoleFlags = $roleFlags",
            roles: List<String>? = null,
            selectionFlags: Int = 0
        ) =
            testCase(
                name = name,
                trackType = C.TRACK_TYPE_AUDIO,
                format = {
                    setRoleFlags(roleFlags)
                    setSelectionFlags(selectionFlags)
                },
                mediaTrack = {
                    setRoles(roles?.sorted())
                }
            )

        private fun videoRoleTestCase(
            roleFlags: Int,
            name: String = "VideoTrackRoleFlags = $roleFlags",
            roles: List<String>? = null,
            selectionFlags: Int = 0
        ) =
            testCase(
                name = name,
                trackType = C.TRACK_TYPE_VIDEO,
                format = {
                    setRoleFlags(roleFlags)
                    setSelectionFlags(selectionFlags)
                },
                mediaTrack = {
                    setRoles(roles?.sorted())
                }
            )

        @JvmStatic
        @Parameterized.Parameters(name = "{index} - {0}")
        fun parameters(): Iterable<Any> {
            return listOf(
                testCase(name = "Unknown", trackType = C.TRACK_TYPE_UNKNOWN),
                testCase(name = "Unknown with sample MimeType", trackType = C.TRACK_TYPE_UNKNOWN, format = {
                    setSampleMimeType("application/custom")
                }, mediaTrack = {
                    setContentType("application/custom")
                }),
                // Text Tracks
                testCase(
                    name = "TextTrack without content type",
                    trackType = C.TRACK_TYPE_TEXT,
                    format = {
                        setSampleMimeType(MimeTypes.APPLICATION_MEDIA3_CUES)
                    }
                ),
                testCase(
                    name = "TextTrack with content type",
                    trackType = C.TRACK_TYPE_TEXT,
                    format = {
                        setSampleMimeType(MimeTypes.APPLICATION_MEDIA3_CUES)
                        setCodecs("${MimeTypes.APPLICATION_TTML} dummy.codecs")
                    },
                    mediaTrack = {
                        setContentType(MimeTypes.APPLICATION_TTML)
                    }
                ),
                testCase(
                    name = "AudioTrack with content type",
                    trackType = C.TRACK_TYPE_AUDIO,
                    format = {
                        setSampleMimeType(MimeTypes.AUDIO_AAC)
                    },
                    mediaTrack = {
                        setContentType(MimeTypes.AUDIO_AAC)
                    }
                ),
                testCase(
                    name = "VideoTrack with content type",
                    trackType = C.TRACK_TYPE_VIDEO,
                    format = {
                        setSampleMimeType(MimeTypes.VIDEO_H263)
                    },
                    mediaTrack = {
                        setContentType(MimeTypes.VIDEO_H263)
                    }
                ),

                textRoleTestCase(roleFlags = C.ROLE_FLAG_MAIN, roles = listOf(MediaTrack.ROLE_MAIN)),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_ALTERNATE, roles = listOf(MediaTrack.ROLE_ALTERNATE)),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_SUPPLEMENTARY, roles = listOf(MediaTrack.ROLE_SUPPLEMENTARY)),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_COMMENTARY, roles = listOf(MediaTrack.ROLE_COMMENTARY)),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_DUB, roles = listOf(MediaTrack.ROLE_DUB)),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_EMERGENCY),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_CAPTION),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_SUBTITLE, subType = MediaTrack.SUBTYPE_SUBTITLES, roles = listOf(MediaTrack.ROLE_SUBTITLE)),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_SIGN),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_DESCRIBES_VIDEO),
                textRoleTestCase(
                    roleFlags = C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND,
                    subType = MediaTrack.SUBTYPE_CAPTIONS,
                    roles = listOf(MediaTrack.ROLE_DESCRIPTION)
                ),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_ENHANCED_DIALOG_INTELLIGIBILITY),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_TRANSCRIBES_DIALOG, subType = MediaTrack.SUBTYPE_DESCRIPTIONS),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_EASY_TO_READ),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_TRICK_PLAY),
                textRoleTestCase(roleFlags = C.ROLE_FLAG_AUXILIARY),
                textRoleTestCase(
                    name = "Forced subtitles",
                    roleFlags = 0,
                    roles = listOf(MediaTrack.ROLE_FORCED_SUBTITLE),
                    selectionFlags = C.SELECTION_FLAG_FORCED
                ),
                textRoleTestCase(
                    name = "TextTrack with multiple roles",
                    roleFlags = C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND or C.ROLE_FLAG_ALTERNATE,
                    subType = MediaTrack.SUBTYPE_CAPTIONS,
                    roles = listOf(
                        MediaTrack.ROLE_ALTERNATE,
                        MediaTrack.ROLE_DESCRIPTION
                    )
                ),
                // Audio
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_MAIN, roles = listOf(MediaTrack.ROLE_MAIN)),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_ALTERNATE, roles = listOf(MediaTrack.ROLE_ALTERNATE)),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_SUPPLEMENTARY, roles = listOf(MediaTrack.ROLE_SUPPLEMENTARY)),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_COMMENTARY, roles = listOf(MediaTrack.ROLE_COMMENTARY)),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_DUB, roles = listOf(MediaTrack.ROLE_DUB)),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_EMERGENCY, roles = listOf(MediaTrack.ROLE_EMERGENCY)),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_CAPTION),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_SUBTITLE),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_SIGN),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_DESCRIBES_VIDEO, roles = listOf(MediaTrack.ROLE_DESCRIPTION)),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_ENHANCED_DIALOG_INTELLIGIBILITY),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_TRANSCRIBES_DIALOG),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_EASY_TO_READ),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_TRICK_PLAY),
                audioRoleTestCase(roleFlags = C.ROLE_FLAG_AUXILIARY),
                // Video
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_MAIN, roles = listOf(MediaTrack.ROLE_MAIN)),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_ALTERNATE, roles = listOf(MediaTrack.ROLE_ALTERNATE)),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_SUPPLEMENTARY, roles = listOf(MediaTrack.ROLE_SUPPLEMENTARY)),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_COMMENTARY),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_DUB),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_EMERGENCY, roles = listOf(MediaTrack.ROLE_EMERGENCY)),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_CAPTION, roles = listOf(MediaTrack.ROLE_CAPTION)),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_SUBTITLE, roles = listOf(MediaTrack.ROLE_SUBTITLE)),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_SIGN, roles = listOf(MediaTrack.ROLE_SIGN)),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_DESCRIBES_VIDEO),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_ENHANCED_DIALOG_INTELLIGIBILITY),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_TRANSCRIBES_DIALOG),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_EASY_TO_READ),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_TRICK_PLAY),
                videoRoleTestCase(roleFlags = C.ROLE_FLAG_AUXILIARY),
            )
        }
    }
}
