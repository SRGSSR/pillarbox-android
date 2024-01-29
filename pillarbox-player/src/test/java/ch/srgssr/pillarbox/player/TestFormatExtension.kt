/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.Format
import ch.srgssr.pillarbox.player.extension.hasRole
import ch.srgssr.pillarbox.player.extension.hasSelection
import org.junit.Assert
import org.junit.Test

class TestFormatExtension {

    @Test
    fun testHasRoleEmpty() {
        val format = Format.Builder().build()
        Assert.assertFalse(format.hasRole(C.ROLE_FLAG_DESCRIBES_VIDEO))
        Assert.assertFalse(format.hasRole(C.ROLE_FLAG_ALTERNATE))
        Assert.assertFalse(format.hasRole(C.ROLE_FLAG_ALTERNATE or C.ROLE_FLAG_DESCRIBES_VIDEO))
        Assert.assertFalse(format.hasRole(C.ROLE_FLAG_SIGN))
    }

    @Test
    fun testHasRole() {
        val roleFlags = C.ROLE_FLAG_ALTERNATE or C.ROLE_FLAG_DESCRIBES_VIDEO
        val format = Format.Builder().setRoleFlags(roleFlags).build()

        Assert.assertTrue(format.hasRole(C.ROLE_FLAG_DESCRIBES_VIDEO))
        Assert.assertTrue(format.hasRole(C.ROLE_FLAG_ALTERNATE))
        Assert.assertTrue(format.hasRole(C.ROLE_FLAG_ALTERNATE or C.ROLE_FLAG_DESCRIBES_VIDEO))
        Assert.assertFalse(format.hasRole(C.ROLE_FLAG_SIGN))
    }

    @Test
    fun testSelectionEmptyFlags() {
        val format = Format.Builder().setSelectionFlags(0).build()
        Assert.assertFalse(format.hasSelection(C.SELECTION_FLAG_DEFAULT))
        Assert.assertFalse(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT))
        Assert.assertFalse(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT))
        Assert.assertFalse(format.hasSelection(C.SELECTION_FLAG_FORCED))
    }

    @Test
    fun testSelectionFlags() {
        val selectionFlags = C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT
        val format = Format.Builder().setSelectionFlags(selectionFlags).build()
        Assert.assertTrue(format.hasSelection(C.SELECTION_FLAG_DEFAULT))
        Assert.assertTrue(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT))
        Assert.assertTrue(format.hasSelection(C.SELECTION_FLAG_AUTOSELECT or C.SELECTION_FLAG_DEFAULT))
        Assert.assertFalse(format.hasSelection(C.SELECTION_FLAG_FORCED))
    }
}
