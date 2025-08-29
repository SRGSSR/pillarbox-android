/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.C
import androidx.media3.common.Format
import com.google.android.gms.cast.MediaTrack

/**
 * Interface responsible for converting between ExoPlayer's [Format] and Cast's [MediaTrack] representations.
 */
interface FormatConverter {

    /**
     * Convert a [Format] to a [MediaTrack] respecting the following specifications https://developers.google.com/cast/docs/reference/web_receiver/cast.framework.messages.Track#roles.
     */
    fun toMediaTrack(trackType: @C.TrackType Int, trackId: Long, format: Format): MediaTrack

    /**
     * Convert a [MediaTrack] to a [Format].
     */
    fun toFormat(mediaTrack: MediaTrack): Format
}
