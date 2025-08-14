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

/**
 * Convert a [MediaTrack] to a [Format] respecting the following specifications https://developers.google.com/cast/docs/reference/web_receiver/cast.framework.messages.Track#roles.
 */
internal fun MediaTrack.toFormat(): Format {
    val builder = Format.Builder()
    // Cast sends a contentType that isn't compatible with exoplayer Track type parsing.
    val sampleMimeType = if (MimeTypes.getTrackType(contentType) == C.TRACK_TYPE_UNKNOWN) {
        when (type) {
            MediaTrack.TYPE_AUDIO -> MimeTypes.AUDIO_UNKNOWN
            MediaTrack.TYPE_VIDEO -> MimeTypes.VIDEO_UNKNOWN
            MediaTrack.TYPE_TEXT -> MimeTypes.TEXT_UNKNOWN
            else -> contentType
        }
    } else {
        contentType
    }
    return builder
        .setId(contentId)
        .setLanguage(language)
        .setLabel(name)
        .setRoleFlags(toRoleFlags())
        .setSelectionFlags(toSelectionFlags())
        .setSampleMimeType(sampleMimeType)
        .build()
}

internal fun MediaTrack.toTrackGroup() = TrackGroup(id.toString(), toFormat())

/**
 * https://developers.google.com/cast/docs/reference/web_receiver/cast.framework.messages.Track#roles
 *
 * The role(s) of the track.
 * The following values for each media type are recognized, with value explanations described in ISO/IEC 23009-1, labeled "DASH role scheme":
 *
 * VIDEO: main, alternate, supplementary, subtitle, emergency, caption, sign
 * AUDIO: main, alternate, supplementary, commentary, dub, emergency
 * TEXT: main, alternate, supplementary, subtitle, commentary, dub, description, forced_subtitle
 *
 * Currently, GoogleCast api doesn't allow correctly identifying audio description track.
 */
@Suppress("CyclomaticComplexMethod")
internal fun MediaTrack.toRoleFlags(): Int {
    var roleFlag = 0
    roles?.forEach { role ->
        roleFlag = roleFlag or when (role) {
            MediaTrack.ROLE_MAIN -> C.ROLE_FLAG_MAIN
            MediaTrack.ROLE_ALTERNATE -> C.ROLE_FLAG_ALTERNATE
            MediaTrack.ROLE_SUPPLEMENTARY -> C.ROLE_FLAG_SUPPLEMENTARY
            MediaTrack.ROLE_SUBTITLE -> C.ROLE_FLAG_SUBTITLE
            MediaTrack.ROLE_DUB -> C.ROLE_FLAG_DUB
            MediaTrack.ROLE_SIGN -> C.ROLE_FLAG_SIGN
            MediaTrack.ROLE_CAPTION -> C.ROLE_FLAG_CAPTION
            MediaTrack.ROLE_COMMENTARY -> C.ROLE_FLAG_COMMENTARY
            MediaTrack.ROLE_EMERGENCY -> C.ROLE_FLAG_EMERGENCY
            // TextTrack only
            MediaTrack.ROLE_DESCRIPTION -> C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND
            MediaTrack.ROLE_FORCED_SUBTITLE -> 0
            else -> 0
        }
    }
    if (type == MediaTrack.TYPE_TEXT && subtype != MediaTrack.SUBTYPE_NONE) {
        when (subtype) {
            MediaTrack.SUBTYPE_CAPTIONS -> roleFlag = roleFlag or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND
            MediaTrack.SUBTYPE_SUBTITLES -> roleFlag = roleFlag or C.ROLE_FLAG_SUBTITLE
            MediaTrack.SUBTYPE_DESCRIPTIONS -> roleFlag = roleFlag or C.ROLE_FLAG_TRANSCRIBES_DIALOG
            else -> Unit
        }
    }
    return roleFlag
}

internal fun MediaTrack.toSelectionFlags(): Int {
    if (type != MediaTrack.TYPE_TEXT) return 0
    return if (roles?.contains(MediaTrack.ROLE_FORCED_SUBTITLE) == true) C.SELECTION_FLAG_FORCED else 0
}
