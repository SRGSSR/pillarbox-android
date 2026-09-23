/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.collection.IntSet
import androidx.collection.intSetOf
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import ch.srgssr.pillarbox.player.extension.hasRole
import ch.srgssr.pillarbox.player.extension.hasSelection
import com.google.android.gms.cast.MediaTrack

/**
 * A default implementation of [FormatConverter].
 */
class DefaultFormatConverter : FormatConverter {

    @Suppress("CyclomaticComplexMethod")
    override fun toMediaTrack(
        trackType: @C.TrackType Int,
        trackId: Long,
        format: Format
    ): MediaTrack {
        val mediaTrackType = when (trackType) {
            C.TRACK_TYPE_TEXT -> MediaTrack.TYPE_TEXT
            C.TRACK_TYPE_VIDEO -> MediaTrack.TYPE_VIDEO
            C.TRACK_TYPE_AUDIO -> MediaTrack.TYPE_AUDIO
            else -> MediaTrack.TYPE_UNKNOWN
        }
        val isTextTrack = trackType == C.TRACK_TYPE_TEXT
        val listRoles = mutableListOf<String>()
        for (mapping in roleMappings) {
            if (mapping.applicableTrackTypes.contains(trackType) && format.hasRole(mapping.media3RoleFlag)) {
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
        val contentType = when (trackType) {
            C.TRACK_TYPE_TEXT -> {
                // Note: Media3 sampleMimeType = MimeTypes.APPLICATION_MEDIA3_CUES, they put the mime type inside codecs with a space separator.
                format.codecs?.split(" ")?.firstOrNull()
            }

            else -> format.sampleMimeType
        }

        return MediaTrack.Builder(trackId, mediaTrackType).apply {
            setLanguage(format.language)
            setContentType(contentType)
            setName(format.label)
            setContentId(format.id)
            textTrackSubType?.let {
                setSubtype(it)
            }
            if (listRoles.isNotEmpty()) {
                setRoles(listRoles.sorted())
            }
        }.build()
    }

    override fun toFormat(mediaTrack: MediaTrack): Format {
        val builder = Format.Builder()
        // Cast sends a contentType that isn't compatible with exoplayer Track type parsing.
        val sampleMimeType = if (MimeTypes.getTrackType(mediaTrack.contentType) == C.TRACK_TYPE_UNKNOWN) {
            when (mediaTrack.type) {
                MediaTrack.TYPE_AUDIO -> MimeTypes.AUDIO_UNKNOWN
                MediaTrack.TYPE_VIDEO -> MimeTypes.VIDEO_UNKNOWN
                MediaTrack.TYPE_TEXT -> MimeTypes.TEXT_UNKNOWN
                else -> mediaTrack.contentType
            }
        } else {
            mediaTrack.contentType
        }
        return builder
            .setId(mediaTrack.contentId)
            .setLanguage(mediaTrack.language)
            .setLabel(mediaTrack.name)
            .setRoleFlags(mediaTrack.toRoleFlags())
            .setSelectionFlags(mediaTrack.toSelectionFlags())
            .setSampleMimeType(sampleMimeType)
            .build()
    }

    internal companion object {
        private val AllTracks = intSetOf(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT)

        // Define role mappings with conditions
        internal data class RoleMapping(
            val media3RoleFlag: Int,
            val mediaTrackRole: String,
            val applicableTrackTypes: IntSet = AllTracks
        )

        internal val roleMappings = listOf(
            RoleMapping(C.ROLE_FLAG_MAIN, MediaTrack.ROLE_MAIN),
            RoleMapping(C.ROLE_FLAG_ALTERNATE, MediaTrack.ROLE_ALTERNATE),
            RoleMapping(C.ROLE_FLAG_SUPPLEMENTARY, MediaTrack.ROLE_SUPPLEMENTARY),
            RoleMapping(C.ROLE_FLAG_COMMENTARY, MediaTrack.ROLE_COMMENTARY, intSetOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT)),
            RoleMapping(C.ROLE_FLAG_DUB, MediaTrack.ROLE_DUB, intSetOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT)),
            RoleMapping(C.ROLE_FLAG_EMERGENCY, MediaTrack.ROLE_EMERGENCY, intSetOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_VIDEO)),
            RoleMapping(C.ROLE_FLAG_CAPTION, MediaTrack.ROLE_CAPTION, intSetOf(C.TRACK_TYPE_VIDEO)),
            RoleMapping(C.ROLE_FLAG_SUBTITLE, MediaTrack.ROLE_SUBTITLE, intSetOf(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_TEXT)),
            RoleMapping(C.ROLE_FLAG_SIGN, MediaTrack.ROLE_SIGN, intSetOf(C.TRACK_TYPE_VIDEO)),
            RoleMapping(C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND, MediaTrack.ROLE_DESCRIPTION, intSetOf(C.TRACK_TYPE_TEXT)),
            RoleMapping(C.ROLE_FLAG_DESCRIBES_VIDEO, MediaTrack.ROLE_DESCRIPTION, intSetOf(C.TRACK_TYPE_AUDIO)),
            // Note: MediaTrack.ROLE_FORCED_SUBTITLE is handle by the selection flags instead of role flags.
        )

        /**
         * https://developers.google.com/cast/docs/reference/web_receiver/cast.framework.messages.Track#roles
         *
         * The role(s) of the track.
         * The following values for each media type are recognized, with value explanations described in ISO/IEC 23009-1, labeled "DASH role scheme":
         *
         * VIDEO: main, alternate, supplementary, subtitle, emergency, caption, sign
         * AUDIO: main, alternate, supplementary, commentary, dub, emergency, description (Pillarbox only)
         * TEXT: main, alternate, supplementary, subtitle, commentary, dub, description, forced_subtitle
         *
         * Currently, GoogleCast api doesn't allow correctly identifying audio description track.
         */
        @Suppress("CyclomaticComplexMethod")
        internal fun MediaTrack.toRoleFlags(): Int {
            var roleFlag = 0
            val media3Type = when (type) {
                MediaTrack.TYPE_AUDIO -> C.TRACK_TYPE_AUDIO
                MediaTrack.TYPE_VIDEO -> C.TRACK_TYPE_VIDEO
                MediaTrack.TYPE_TEXT -> C.TRACK_TYPE_TEXT
                else -> C.TRACK_TYPE_UNKNOWN
            }
            roles?.forEach { role ->
                roleFlag = roleFlag or when (role) {
                    MediaTrack.ROLE_DESCRIPTION -> when (type) {
                        MediaTrack.TYPE_AUDIO -> C.ROLE_FLAG_DESCRIBES_VIDEO
                        MediaTrack.TYPE_TEXT -> C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND
                        else -> 0
                    }

                    MediaTrack.ROLE_FORCED_SUBTITLE -> 0 // Handled by selectionFlags
                    else -> roleMappings.find { it.mediaTrackRole == role && media3Type in it.applicableTrackTypes }?.media3RoleFlag ?: 0
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
    }
}
