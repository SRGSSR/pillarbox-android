/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.extension

import androidx.media3.common.C
import ch.srgssr.pillarbox.player.extension.hasRole
import ch.srgssr.pillarbox.player.extension.hasSelection
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.Track
import ch.srgssr.pillarbox.player.tracks.VideoTrack
import com.google.android.gms.cast.MediaTrack

/**
 * Convert a [Track] to a [MediaTrack] respecting the following specifications https://developers.google.com/cast/docs/reference/web_receiver/cast.framework.messages.Track#roles.
 * @param trackId The id of the track.
 *
 *
 * The role(s) of the track.
 * The following values for each media type are recognized, with value explanations described in ISO/IEC 23009-1, labeled "DASH role scheme":
 *
 * VIDEO: main, alternate, supplementary, subtitle, emergency, caption, sign
 * AUDIO: main, alternate, supplementary, commentary, dub, emergency
 * TEXT: main, alternate, supplementary, subtitle, commentary, dub, description, forced_subtitle
 */
@Suppress("CyclomaticComplexMethod")
internal fun Track.toMediaTrack(trackId: Long): MediaTrack {
    val type = when (this) {
        is TextTrack -> MediaTrack.TYPE_TEXT
        is AudioTrack -> MediaTrack.TYPE_AUDIO
        is VideoTrack -> MediaTrack.TYPE_VIDEO
    }
    val isTextTrack = type == MediaTrack.TYPE_TEXT
    val isAudioTrack = type == MediaTrack.TYPE_AUDIO
    val isVideoTrack = type == MediaTrack.TYPE_VIDEO
    val listRoles = mutableListOf<String>()
    if (format.hasRole(C.ROLE_FLAG_MAIN)) {
        listRoles.add(MediaTrack.ROLE_MAIN)
    }
    if (format.hasRole(C.ROLE_FLAG_ALTERNATE)) {
        listRoles.add(MediaTrack.ROLE_ALTERNATE)
    }
    if (format.hasRole(C.ROLE_FLAG_SUPPLEMENTARY)) {
        listRoles.add(MediaTrack.ROLE_SUPPLEMENTARY)
    }
    if ((isAudioTrack || isTextTrack) && format.hasRole(C.ROLE_FLAG_COMMENTARY)) {
        listRoles.add(MediaTrack.ROLE_COMMENTARY)
    }
    if ((isAudioTrack || isTextTrack) && format.hasRole(C.ROLE_FLAG_DUB)) {
        listRoles.add(MediaTrack.ROLE_DUB)
    }
    if ((isAudioTrack || isVideoTrack) && format.hasRole(C.ROLE_FLAG_EMERGENCY)) {
        listRoles.add(MediaTrack.ROLE_EMERGENCY)
    }
    if (isVideoTrack && format.hasRole(C.ROLE_FLAG_CAPTION)) {
        listRoles.add(MediaTrack.ROLE_CAPTION)
    }
    if ((isTextTrack || isVideoTrack) && format.hasRole(C.ROLE_FLAG_SUBTITLE)) {
        listRoles.add(MediaTrack.ROLE_SUBTITLE)
    }
    if (isVideoTrack && format.hasRole(C.ROLE_FLAG_SIGN)) {
        listRoles.add(MediaTrack.ROLE_SIGN)
    }
    if (isTextTrack && format.hasRole(C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND)) {
        listRoles.add(MediaTrack.ROLE_DESCRIPTION)
    }
    if (isTextTrack && format.hasSelection(C.SELECTION_FLAG_FORCED)) {
        listRoles.add(MediaTrack.ROLE_FORCED_SUBTITLE)
    }

    val textTrackSubType: Int? = when {
        !isTextTrack -> null
        format.hasRole(C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND) -> MediaTrack.SUBTYPE_CAPTIONS
        format.hasRole(C.ROLE_FLAG_TRANSCRIBES_DIALOG) -> MediaTrack.SUBTYPE_DESCRIPTIONS
        format.hasRole(C.ROLE_FLAG_SUBTITLE) -> MediaTrack.SUBTYPE_SUBTITLES
        else -> MediaTrack.SUBTYPE_NONE
    }
    return MediaTrack.Builder(trackId, type).apply {
        setLanguage(format.language)
        setContentType(if (isTextTrack) format.containerMimeType else format.sampleMimeType)
        setName(format.label)
        setContentId(format.id)
        textTrackSubType?.let {
            setSubtype(it)
        }
        setRoles(listRoles)
    }.build()
}
