/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.VideoSize
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class FormatTest {
    @Test
    fun `hasRole with empty roles`() {
        val format = Format.Builder().build()

        assertFalse(format.hasRole(C.ROLE_FLAG_DESCRIBES_VIDEO))
        assertFalse(format.hasRole(C.ROLE_FLAG_ALTERNATE))
        assertFalse(format.hasRole(C.ROLE_FLAG_ALTERNATE or C.ROLE_FLAG_DESCRIBES_VIDEO))
        assertFalse(format.hasRole(C.ROLE_FLAG_SIGN))
    }

    @Test
    fun `hasRole with roles`() {
        val roleFlags = C.ROLE_FLAG_ALTERNATE or C.ROLE_FLAG_DESCRIBES_VIDEO
        val format = Format.Builder().setRoleFlags(roleFlags).build()

        assertTrue(format.hasRole(C.ROLE_FLAG_DESCRIBES_VIDEO))
        assertTrue(format.hasRole(C.ROLE_FLAG_ALTERNATE))
        assertTrue(format.hasRole(C.ROLE_FLAG_ALTERNATE or C.ROLE_FLAG_DESCRIBES_VIDEO))
        assertFalse(format.hasRole(C.ROLE_FLAG_SIGN))
    }

    @Test
    fun `hasSelection with empty selections`() {
        val format = Format.Builder().build()

        assertFalse(format.hasSelection(C.SELECTION_FLAG_DEFAULT))
        assertFalse(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT))
        assertFalse(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT))
        assertFalse(format.hasSelection(C.SELECTION_FLAG_FORCED))
    }

    @Test
    fun `hasSelection with selections`() {
        val selectionFlags = C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT
        val format = Format.Builder().setSelectionFlags(selectionFlags).build()

        assertTrue(format.hasSelection(C.SELECTION_FLAG_DEFAULT))
        assertTrue(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT))
        assertTrue(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT))
        assertFalse(format.hasSelection(C.SELECTION_FLAG_FORCED))
    }

    @Test
    fun `is forced, selection not set`() {
        val format = Format.Builder().build()

        assertFalse(format.isForced())
    }

    @Test
    fun `is forced, selection set`() {
        val selectionFlags = C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_FORCED
        val format = Format.Builder().setSelectionFlags(selectionFlags).build()

        assertTrue(format.isForced())
    }

    @Test
    fun `videoSize with no dimension set`() {
        val format = Format.Builder().build()

        assertEquals(VideoSize.UNKNOWN, format.videoSize)
    }

    @Test
    fun `videoSize with width only`() {
        val format = Format.Builder().setWidth(1440).build()

        assertEquals(VideoSize.UNKNOWN, format.videoSize)
    }

    @Test
    fun `videoSize with height only`() {
        val format = Format.Builder().setHeight(900).build()

        assertEquals(VideoSize.UNKNOWN, format.videoSize)
    }

    @Test
    fun `videoSize with both dimensions set`() {
        val width = 1440
        val height = 900
        val format = Format.Builder().setWidth(width).setHeight(height).build()

        assertEquals(VideoSize(width, height), format.videoSize)
    }

    @Test
    fun `selectionString with empty selections`() {
        val format = Format.Builder().build()

        assertEquals("", format.selectionString())
    }

    @Test
    fun `selectionString with some selections`() {
        val format = Format.Builder()
            .setSelectionFlags(C.SELECTION_FLAG_FORCED or C.SELECTION_FLAG_DEFAULT)
            .build()

        assertEquals("default,forced", format.selectionString())
    }

    @Test
    fun `selectionString with all selections`() {
        val format = Format.Builder()
            .setSelectionFlags(C.SELECTION_FLAG_FORCED or C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT)
            .build()

        assertEquals("auto,default,forced", format.selectionString())
    }

    @Test
    fun `roleString with empty roles`() {
        val format = Format.Builder().build()

        assertEquals("", format.roleString())
    }

    @Test
    fun `roleString with some roles`() {
        val format = Format.Builder()
            .setRoleFlags(C.ROLE_FLAG_EMERGENCY or C.ROLE_FLAG_MAIN or C.ROLE_FLAG_TRICK_PLAY)
            .build()

        assertEquals("main,emergency,trick-play", format.roleString())
    }

    @Test
    fun `roleString with all roles`() {
        val format = Format.Builder()
            .setRoleFlags(
                C.ROLE_FLAG_MAIN or
                    C.ROLE_FLAG_ALTERNATE or
                    C.ROLE_FLAG_SUPPLEMENTARY or
                    C.ROLE_FLAG_COMMENTARY or
                    C.ROLE_FLAG_DUB or
                    C.ROLE_FLAG_EMERGENCY or
                    C.ROLE_FLAG_CAPTION or
                    C.ROLE_FLAG_SUBTITLE or
                    C.ROLE_FLAG_SIGN or
                    C.ROLE_FLAG_DESCRIBES_VIDEO or
                    C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND or
                    C.ROLE_FLAG_ENHANCED_DIALOG_INTELLIGIBILITY or
                    C.ROLE_FLAG_TRANSCRIBES_DIALOG or
                    C.ROLE_FLAG_EASY_TO_READ or
                    C.ROLE_FLAG_TRICK_PLAY or
                    C.ROLE_FLAG_AUXILIARY
            )
            .build()

        assertEquals(
            "main,alt,supplementary,commentary,dub,emergency,caption,subtitle,sign,describes-video,describes-music," +
                "enhanced-intelligibility,transcribes-dialog,easy-read,trick-play,auxiliary",
            format.roleString()
        )
    }

    @Test
    fun `hasAccessibilityRoles with empty roles`() {
        val format = Format.Builder().build()

        assertFalse(format.hasAccessibilityRoles())
    }

    @Test
    fun `hasAccessibilityRoles without accessibility roles`() {
        val format = Format.Builder()
            .setRoleFlags(C.ROLE_FLAG_EMERGENCY or C.ROLE_FLAG_MAIN)
            .build()

        assertFalse(format.hasAccessibilityRoles())
    }

    @Test
    fun `hasAccessibilityRoles with accessibility roles`() {
        val format = Format.Builder()
            .setRoleFlags(C.ROLE_FLAG_EMERGENCY or C.ROLE_FLAG_DESCRIBES_VIDEO)
            .build()

        assertTrue(format.hasAccessibilityRoles())
    }

    @Test
    fun `getLocale without language`() {
        val format = Format.Builder().build()

        assertNull(format.getLocale())
    }

    @Test
    fun `getLocale with language`() {
        val format = Format.Builder().setLanguage("fr_CH").build()

        assertEquals(Locale("fr", "CH"), format.getLocale())
    }

    @Test
    fun `getLocale with invalid language`() {
        val format = Format.Builder().setLanguage("xx_YY").build()

        assertEquals(Locale("xx", "YY"), format.getLocale())
    }

    @Test
    fun `displayName without language, without label`() {
        val format = Format.Builder().build()

        assertEquals(C.LANGUAGE_UNDETERMINED, format.displayName)
    }

    @Test
    fun `displayName with language, without label`() {
        val format = Format.Builder().setLanguage("fr").build()

        assertEquals("French", format.displayName)
    }

    @Test
    fun `displayName without language, with label`() {
        val format = Format.Builder().setLabel("Format label").build()

        assertEquals("Format label", format.displayName)
    }

    @Test
    fun `displayName with language, with label`() {
        val format = Format.Builder()
            .setLanguage("fr")
            .setLabel("Format label")
            .build()

        assertEquals("French", format.displayName)
    }
}
