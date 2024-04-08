/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.Tracks

/**
 * Text tracks.
 */
val Tracks.text: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_TEXT)

/**
 * Audio tracks.
 */
val Tracks.audio: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_AUDIO)

/**
 * Video tracks.
 */
val Tracks.video: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_VIDEO)

private fun Tracks.filterByTrackType(trackType: @TrackType Int): List<Tracks.Group> {
    return groups.filter { it.type == trackType }
}
