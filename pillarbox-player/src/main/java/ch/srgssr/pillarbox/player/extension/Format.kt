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
 * Checks if this [Format] has the specified [role].
 *
 * @param role The role to check for, represented by a [C.RoleFlags] value.
 * @return Whether this [Format] has the specified role.
 */
fun Format.hasRole(role: @RoleFlags Int): Boolean {
    return roleFlags and role != 0
}

/**
 * Checks if this [Format] has the specified [selection flags][selection].
 *
 * @param selection The selection flags to check for, represented by a combination of values from [C.SelectionFlags].
 * @return Whether this [Format] has the specified selection flags.
 */
fun Format.hasSelection(selection: @SelectionFlags Int): Boolean {
    return selectionFlags and selection != 0
}

/**
 * Checks if this [Format] is forced.
 *
 * @return Whether this [Format] is forced.
 * @see C.SELECTION_FLAG_FORCED
 */
fun Format.isForced(): Boolean {
    return hasSelection(C.SELECTION_FLAG_FORCED)
}

/**
 * Returns the video size of this [Format].
 *
 * @return The video size of the format or [VideoSize.UNKNOWN] if not available.
 */
val Format.videoSize: VideoSize
    get() {
        if (width != Format.NO_VALUE && height != Format.NO_VALUE) {
            return VideoSize(width, height)
        }
        return VideoSize.UNKNOWN
    }

/**
 * Returns a string representation of the selection flags associated with this [Format].
 *
 * @return A string representation of the selection flags, or an empty string if none are set.
 */
fun Format.selectionString(): String {
    val selectionFlags = mutableListOf<String>()
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
 * Returns a string representation of the role flags set for this [Format].
 *
 * @return A string representing the role flags, or an empty string if none are set.
 */
@Suppress("CyclomaticComplexMethod")
fun Format.roleString(): String {
    val roleFlags = mutableListOf<String>()
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
    if (hasRole(C.ROLE_FLAG_AUXILIARY)) {
        roleFlags.add("auxiliary")
    }
    return roleFlags.joinToString(",")
}

/**
 * Checks if this [Format] has accessibility roles, specifically if it describes video or sound.
 *
 * @return Whether this [Format] has accessibility roles.
 */
fun Format.hasAccessibilityRoles(): Boolean {
    return hasRole(C.ROLE_FLAG_DESCRIBES_VIDEO or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND)
}

/**
 * Returns a [Locale] representing the language specified by this [Format]'s [language][Format.language] property.
 *
 * @return A [Locale] corresponding to the [language][Format.language] tag, or `null` if not available.
 */
fun Format.getLocale(): Locale? {
    return language?.let { Locale.forLanguageTag(it) }
}

/**
 * The display name for this [Format].
 *
 * @return The display name of this [Format].
 */
val Format.displayName: String
    get() = getLocale()?.displayName ?: label ?: C.LANGUAGE_UNDETERMINED
