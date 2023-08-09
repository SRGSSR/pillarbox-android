/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks

/**
 * Text tracks
 */
val Tracks.text: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_TEXT).mapNotNull { it.filterForced() }

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

@Suppress("SpreadOperator", "ReturnCount")
private fun Tracks.Group.filterForced(): Tracks.Group? {
    if (type != C.TRACK_TYPE_TEXT) return this
    if (length == 1 && getTrackFormat(0).isForced()) return null
    val listIndexNotForced = ArrayList<Int>(length)
    for (i in 0 until length) {
        val track = getTrackFormat(i)
        if (!track.isForced()) {
            listIndexNotForced.add(i)
        }
    }
    if (listIndexNotForced.size == length) return this
    val count = listIndexNotForced.size
    val formats = Array(count) {
        getTrackFormat(listIndexNotForced[it])
    }
    val trackSupport = Array(count) {
        getTrackSupport(listIndexNotForced[it])
    }
    val trackEnabled = Array(count) {
        isTrackSelected(listIndexNotForced[it])
    }
    val trackGroup = TrackGroup(mediaTrackGroup.id, *formats)
    return Tracks.Group(trackGroup, isAdaptiveSupported, trackSupport.toIntArray(), trackEnabled.toBooleanArray())
}
