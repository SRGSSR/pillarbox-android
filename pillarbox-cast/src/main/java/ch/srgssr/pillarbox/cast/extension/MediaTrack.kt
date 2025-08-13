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

internal fun MediaTrack.toFormat(): Format {
    val builder = Format.Builder()
    // Cast sends a contentType that isn't compatible with exoplayer Track type parsing.
    val containerMimeType = if (MimeTypes.getTrackType(contentType) == C.TRACK_TYPE_UNKNOWN) {
        when (type) {
            MediaTrack.TYPE_AUDIO -> MimeTypes.AUDIO_UNKNOWN
            MediaTrack.TYPE_VIDEO -> MimeTypes.VIDEO_UNKNOWN
            MediaTrack.TYPE_TEXT -> MimeTypes.TEXT_UNKNOWN
            else -> contentType
        }
    } else {
        contentType
    }

    var roleFlag = 0
    var selectionFlags = C.SELECTION_FLAG_DEFAULT
    roles?.forEach { role ->
        roleFlag = roleFlag or when (role) {
            MediaTrack.ROLE_MAIN -> C.ROLE_FLAG_MAIN
            MediaTrack.ROLE_SUPPLEMENTARY -> C.ROLE_FLAG_SUPPLEMENTARY
            MediaTrack.ROLE_DUB -> C.ROLE_FLAG_DUB
            MediaTrack.ROLE_SIGN -> C.ROLE_FLAG_SIGN
            MediaTrack.ROLE_CAPTION -> C.ROLE_FLAG_CAPTION
            MediaTrack.ROLE_COMMENTARY -> C.ROLE_FLAG_COMMENTARY
            MediaTrack.ROLE_SUBTITLE -> C.ROLE_FLAG_SUBTITLE
            MediaTrack.ROLE_EMERGENCY -> C.ROLE_FLAG_EMERGENCY
            MediaTrack.ROLE_ALTERNATE -> C.ROLE_FLAG_ALTERNATE
            MediaTrack.ROLE_DESCRIPTION -> C.ROLE_FLAG_DESCRIBES_VIDEO or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND
            else -> 0
        }
        if (role == MediaTrack.ROLE_FORCED_SUBTITLE) {
            selectionFlags = selectionFlags or C.SELECTION_FLAG_FORCED
        }

        when {
            type == MediaTrack.TYPE_AUDIO && subtype == MediaTrack.SUBTYPE_DESCRIPTIONS ->
                roleFlag = roleFlag or C.ROLE_FLAG_DESCRIBES_VIDEO

            type == MediaTrack.TYPE_TEXT && subtype == MediaTrack.SUBTYPE_DESCRIPTIONS ->
                roleFlag = roleFlag or C.ROLE_FLAG_DESCRIBES_VIDEO or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND

            type == MediaTrack.TYPE_TEXT && subtype == MediaTrack.SUBTYPE_SUBTITLES ->
                roleFlag = roleFlag or C.ROLE_FLAG_SUBTITLE

            type == MediaTrack.TYPE_TEXT && subtype == MediaTrack.SUBTYPE_CAPTIONS ->
                roleFlag = roleFlag or C.ROLE_FLAG_CAPTION
        }
    }

    return builder
        .setId(contentId)
        .setLanguage(language)
        .setLabel(name)
        .setRoleFlags(roleFlag)
        .setSelectionFlags(selectionFlags)
        .setContainerMimeType(containerMimeType)
        .build()
}

internal fun MediaTrack.toTrackGroup() = TrackGroup(id.toString(), toFormat())
