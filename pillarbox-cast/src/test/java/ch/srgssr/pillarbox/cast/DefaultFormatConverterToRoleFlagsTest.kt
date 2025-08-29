/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import ch.srgssr.pillarbox.cast.DefaultFormatConverter.Companion.toRoleFlags
import com.google.android.gms.cast.MediaTrack
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class DefaultFormatConverterToRoleFlagsTest(val case: ToRoleFlagsTestCase) {

    @Test
    fun toRoleFlags() {
        val actualRoleFlags = case.mediaTrack.toRoleFlags()
        assertEquals(case.expectedRoleFlag, actualRoleFlags, message = "${case.mediaTrack.roles} failed")
    }

    class ToRoleFlagsTestCase(val mediaTrack: MediaTrack, val expectedRoleFlag: Int) {
        constructor(trackType: Int, roles: List<String>, expectedRoleFlag: Int) : this(
            MediaTrack.Builder(0, trackType).setRoles(roles).build(),
            expectedRoleFlag
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Iterable<Any> {
            return listOf(
                // AUDIO: main, alternate, supplementary, commentary, dub, emergency, description (Pillarbox only)
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_ALTERNATE), C.ROLE_FLAG_ALTERNATE),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_CAPTION), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_COMMENTARY), C.ROLE_FLAG_COMMENTARY),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_DESCRIPTION), C.ROLE_FLAG_DESCRIBES_VIDEO),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_DUB), C.ROLE_FLAG_DUB),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_EMERGENCY), C.ROLE_FLAG_EMERGENCY),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_FORCED_SUBTITLE), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_MAIN), C.ROLE_FLAG_MAIN),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_SIGN), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_SUPPLEMENTARY), C.ROLE_FLAG_SUPPLEMENTARY),
                ToRoleFlagsTestCase(MediaTrack.TYPE_AUDIO, listOf(MediaTrack.ROLE_SUBTITLE), 0),
                ToRoleFlagsTestCase(
                    MediaTrack.TYPE_AUDIO,
                    listOf(MediaTrack.ROLE_MAIN, MediaTrack.ROLE_DESCRIPTION),
                    C.ROLE_FLAG_MAIN or C.ROLE_FLAG_DESCRIBES_VIDEO
                ),
                // TEXT: main, alternate, supplementary, subtitle, commentary, dub, description, forced_subtitle
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_ALTERNATE), C.ROLE_FLAG_ALTERNATE),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_CAPTION), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_COMMENTARY), C.ROLE_FLAG_COMMENTARY),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_DESCRIPTION), C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_DUB), C.ROLE_FLAG_DUB),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_EMERGENCY), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_FORCED_SUBTITLE), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_MAIN), C.ROLE_FLAG_MAIN),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_SIGN), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_SUPPLEMENTARY), C.ROLE_FLAG_SUPPLEMENTARY),
                ToRoleFlagsTestCase(MediaTrack.TYPE_TEXT, listOf(MediaTrack.ROLE_SUBTITLE), C.ROLE_FLAG_SUBTITLE),
                ToRoleFlagsTestCase(
                    MediaTrack.TYPE_TEXT,
                    listOf(MediaTrack.ROLE_DESCRIPTION, MediaTrack.ROLE_ALTERNATE),
                    C.ROLE_FLAG_ALTERNATE or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND
                ),
                ToRoleFlagsTestCase(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setSubtype(MediaTrack.SUBTYPE_CAPTIONS)
                        .build(),
                    C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND
                ),
                ToRoleFlagsTestCase(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                        .build(),
                    C.ROLE_FLAG_SUBTITLE
                ),
                ToRoleFlagsTestCase(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setSubtype(MediaTrack.SUBTYPE_DESCRIPTIONS)
                        .build(),
                    C.ROLE_FLAG_TRANSCRIBES_DIALOG
                ),
                ToRoleFlagsTestCase(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setSubtype(MediaTrack.SUBTYPE_CHAPTERS)
                        .build(),
                    0
                ),
                ToRoleFlagsTestCase(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setSubtype(MediaTrack.SUBTYPE_METADATA)
                        .build(),
                    0
                ),
                ToRoleFlagsTestCase(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setSubtype(MediaTrack.SUBTYPE_NONE)
                        .build(),
                    0
                ),
                ToRoleFlagsTestCase(
                    MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
                        .setSubtype(MediaTrack.SUBTYPE_UNKNOWN)
                        .build(),
                    0
                ),
                // VIDEO: main, alternate, supplementary, subtitle, emergency, caption, sign
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_ALTERNATE), C.ROLE_FLAG_ALTERNATE),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_CAPTION), C.ROLE_FLAG_CAPTION),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_COMMENTARY), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_DESCRIPTION), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_DUB), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_EMERGENCY), C.ROLE_FLAG_EMERGENCY),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_FORCED_SUBTITLE), 0),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_MAIN), C.ROLE_FLAG_MAIN),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_SIGN), C.ROLE_FLAG_SIGN),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_SUPPLEMENTARY), C.ROLE_FLAG_SUPPLEMENTARY),
                ToRoleFlagsTestCase(MediaTrack.TYPE_VIDEO, listOf(MediaTrack.ROLE_SUBTITLE), C.ROLE_FLAG_SUBTITLE),
            )
        }
    }
}
