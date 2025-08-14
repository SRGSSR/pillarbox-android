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
import java.util.Collections

private val AllTracks = hashSetOf(MediaTrack.TYPE_VIDEO, MediaTrack.TYPE_AUDIO, MediaTrack.TYPE_TEXT)
private val VideoTrack = Collections.singleton(MediaTrack.TYPE_VIDEO)
private val AudioTrack = Collections.singleton(MediaTrack.TYPE_VIDEO)
private val TextTrack = Collections.singleton(MediaTrack.TYPE_VIDEO)

// Define role mappings with conditions
private data class RoleMapping(
    val media3RoleFlag: Int,
    val mediaTrackRole: String,
    val setTrackTypeApplicable: Set<Int> = AllTracks
)

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
fun Track.toMediaTrack(trackId: Long): MediaTrack {
    val type = when (this) {
        is TextTrack -> MediaTrack.TYPE_TEXT
        is AudioTrack -> MediaTrack.TYPE_AUDIO
        is VideoTrack -> MediaTrack.TYPE_VIDEO
    }
    val isTextTrack = this is TextTrack
    val listRoles = mutableListOf<String>()
    val roleMappings = listOf(
        RoleMapping(C.ROLE_FLAG_MAIN, MediaTrack.ROLE_MAIN),
        RoleMapping(C.ROLE_FLAG_ALTERNATE, MediaTrack.ROLE_ALTERNATE),
        RoleMapping(C.ROLE_FLAG_SUPPLEMENTARY, MediaTrack.ROLE_SUPPLEMENTARY),
        RoleMapping(C.ROLE_FLAG_COMMENTARY, MediaTrack.ROLE_COMMENTARY, VideoTrack + TextTrack),
        RoleMapping(C.ROLE_FLAG_DUB, MediaTrack.ROLE_DUB, AudioTrack + TextTrack),
        RoleMapping(C.ROLE_FLAG_EMERGENCY, MediaTrack.ROLE_EMERGENCY, AudioTrack + VideoTrack),
        RoleMapping(C.ROLE_FLAG_CAPTION, MediaTrack.ROLE_CAPTION, VideoTrack),
        RoleMapping(C.ROLE_FLAG_SUBTITLE, MediaTrack.ROLE_SUBTITLE, TextTrack + VideoTrack),
        RoleMapping(C.ROLE_FLAG_SIGN, MediaTrack.ROLE_SIGN, VideoTrack),
        RoleMapping(C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND, MediaTrack.ROLE_DESCRIPTION, TextTrack)
        // Note: MediaTrack.ROLE_FORCED_SUBTITLE is handle by the selection flags instead of role flags.
    )
    for (mapping in roleMappings) {
        if (mapping.setTrackTypeApplicable.contains(type) && format.hasRole(mapping.media3RoleFlag)) {
            listRoles.add(mapping.mediaTrackRole)
        }
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
    val contentType = when (this) {
        is TextTrack -> {
            // Note: Media3 sampleMimeType = MimeTypes.APPLICATION_MEDIA3_CUES, they put the mime type inside codecs with a space separator.
            format.codecs?.split(" ")?.firstOrNull()
        }

        is AudioTrack -> format.sampleMimeType
        is VideoTrack -> format.sampleMimeType
    }

    return MediaTrack.Builder(trackId, type).apply {
        setLanguage(format.language)
        setContentType(contentType)
        setName(format.label)
        setContentId(format.id)
        textTrackSubType?.let {
            setSubtype(it)
        }
        setRoles(listRoles)
    }.build()
}
