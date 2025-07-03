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
    val trackType = MimeTypes.getTrackType(contentType)
    val containerMimeType = if (trackType == C.TRACK_TYPE_UNKNOWN && subtype <= MediaTrack.SUBTYPE_DESCRIPTIONS) {
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
        .setContainerMimeType(containerMimeType)
        .build()
}

internal fun MediaTrack.toTrackGroup() = TrackGroup(id.toString(), toFormat())
