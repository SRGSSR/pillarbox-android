/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.extension

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import com.google.android.gms.cast.MediaTrack

const val CAST_TEXT_TRACK = MimeTypes.BASE_TYPE_TEXT + "/cast"

internal fun MediaTrack.toFormat(): Format {
    val builder = Format.Builder()
    if (type == MediaTrack.TYPE_TEXT && MimeTypes.getTrackType(contentType) == C.TRACK_TYPE_UNKNOWN) {
        builder.setSampleMimeType(CAST_TEXT_TRACK)
    }
    return builder
        .setId(contentId)
        .setContainerMimeType(contentType)
        .setLanguage(language)
        .build()
}
