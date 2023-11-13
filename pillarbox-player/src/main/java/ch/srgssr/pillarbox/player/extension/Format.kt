/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.C.RoleFlags
import androidx.media3.common.C.SelectionFlags
import androidx.media3.common.Format
import androidx.media3.common.VideoSize
import java.util.Locale

/**
 * Check if [Format.roleFlags] contains [role]
 *
 * @param role The [C.RoleFlags] to check.
 */
fun Format.hasRole(role: @RoleFlags Int): Boolean {
    return roleFlags and role != 0
}

/**
 * Check if [Format.selectionFlags] contains [selection]
 *
 * @param selection The [C.SelectionFlags] to check.
 */
fun Format.hasSelection(selection: @SelectionFlags Int): Boolean {
    return selectionFlags and selection != 0
}

/**
 * Is forced
 */
fun Format.isForced(): Boolean {
    return hasSelection(C.SELECTION_FLAG_FORCED)
}

/**
 * Video size of the format. [VideoSize.UNKNOWN] if no video size provided.
 */
val Format.videoSize: VideoSize
    get() {
        if (width != Format.NO_VALUE && height != Format.NO_VALUE) {
            return VideoSize(width, height)
        }
        return VideoSize.UNKNOWN
    }

/**
 * Selection flags as string
 */
fun Format.selectionString(): String {
    val selectionFlags: MutableList<String> = ArrayList()
    if (hasSelection(C.SELECTION_FLAG_AUTOSELECT)) {
        selectionFlags.add("auto")
    }
    if (hasSelection(C.SELECTION_FLAG_DEFAULT)) {
        selectionFlags.add("default")
    }
    if (hasSelection(C.SELECTION_FLAG_FORCED)) {
        selectionFlags.add("forced")
    }
    return selectionFlags.joinToString(",")
}

/**
 * Role flags as string
 */
@Suppress("CyclomaticComplexMethod")
fun Format.roleString(): String {
    val roleFlags: MutableList<String> = ArrayList()
    if (hasRole(C.ROLE_FLAG_MAIN)) {
        roleFlags.add("main")
    }
    if (hasRole(C.ROLE_FLAG_ALTERNATE)) {
        roleFlags.add("alt")
    }
    if (hasRole(C.ROLE_FLAG_SUPPLEMENTARY)) {
        roleFlags.add("supplementary")
    }
    if (hasRole(C.ROLE_FLAG_COMMENTARY)) {
        roleFlags.add("commentary")
    }
    if (hasRole(C.ROLE_FLAG_DUB)) {
        roleFlags.add("dub")
    }
    if (hasRole(C.ROLE_FLAG_EMERGENCY)) {
        roleFlags.add("emergency")
    }
    if (hasRole(C.ROLE_FLAG_CAPTION)) {
        roleFlags.add("caption")
    }
    if (hasRole(C.ROLE_FLAG_SUBTITLE)) {
        roleFlags.add("subtitle")
    }
    if (hasRole(C.ROLE_FLAG_SIGN)) {
        roleFlags.add("sign")
    }
    if (hasRole(C.ROLE_FLAG_DESCRIBES_VIDEO)) {
        roleFlags.add("describes-video")
    }
    if (hasRole(C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND)) {
        roleFlags.add("describes-music")
    }
    if (hasRole(C.ROLE_FLAG_ENHANCED_DIALOG_INTELLIGIBILITY)) {
        roleFlags.add("enhanced-intelligibility")
    }
    if (hasRole(C.ROLE_FLAG_TRANSCRIBES_DIALOG)) {
        roleFlags.add("transcribes-dialog")
    }
    if (hasRole(C.ROLE_FLAG_EASY_TO_READ)) {
        roleFlags.add("easy-read")
    }
    if (hasRole(C.ROLE_FLAG_TRICK_PLAY)) {
        roleFlags.add("trick-play")
    }
    return roleFlags.joinToString(",")
}

/**
 * Has accessibility roles
 */
fun Format.hasAccessibilityRoles(): Boolean {
    return hasRole(C.ROLE_FLAG_DESCRIBES_VIDEO or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND)
}

/**
 * Returns a locale for the specified IETF BCP 47 [Format.language] tag string.
 *
 * @return null if not applicable.
 */
fun Format.getLocale(): Locale? {
    return language?.let { Locale.forLanguageTag(it) }
}

/**
 * Display name
 */
val Format.displayName: String
    get() = getLocale()?.displayName ?: label ?: C.LANGUAGE_UNDETERMINED
