/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import ch.srgssr.pillarbox.cast.DefaultFormatConverter.Companion.toSelectionFlags
import com.google.android.gms.cast.MediaTrack
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultFormatAdapterToSelectionFlags {

    @Test
    fun `check empty roles doesn't set selectionFlags`() {
        val mediaTrack = MediaTrack.Builder(0, MediaTrack.TYPE_TEXT).build()
        val actualSelectionFlags = mediaTrack.toSelectionFlags()
        assertEquals(0, actualSelectionFlags)
    }

    @Test
    fun `check forced subtitle for other track types doesn't set selectionFlags`() {
        val mediaTrack = MediaTrack.Builder(0, MediaTrack.TYPE_VIDEO)
            .setRoles(listOf(MediaTrack.ROLE_FORCED_SUBTITLE))
            .build()
        val actualSelectionFlags = mediaTrack.toSelectionFlags()
        assertEquals(0, actualSelectionFlags)
    }

    @Test
    fun `check forced subtitle correctly set selectionFlags`() {
        val mediaTrack = MediaTrack.Builder(0, MediaTrack.TYPE_TEXT)
            .setRoles(listOf(MediaTrack.ROLE_FORCED_SUBTITLE))
            .build()
        val actualSelectionFlags = mediaTrack.toSelectionFlags()
        assertEquals(C.SELECTION_FLAG_FORCED, actualSelectionFlags)
    }
}
