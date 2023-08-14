/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.annotation.SuppressLint
import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.Format
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
    get() = filterByTrackType(C.TRACK_TYPE_AUDIO).mapNotNull { it.filterUnsupported() }

/**
 * Video tracks.
 */
val Tracks.video: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_VIDEO).mapNotNull { it.filterUnsupported() }

private fun Tracks.filterByTrackType(trackType: @TrackType Int): List<Tracks.Group> {
    return groups.filter { it.type == trackType }
}

private fun Tracks.Group.filterForced(): Tracks.Group? {
    return filterBy { group, i -> group.getTrackFormat(i).isForced() }
}

internal fun Tracks.Group.filterUnsupported(): Tracks.Group? {
    return filterBy { group, i -> !group.isTrackSupported(i) }
}

@SuppressLint("WrongConstant")
@Suppress("SpreadOperator", "ReturnCount")
internal fun Tracks.Group.filterBy(filter: (Tracks.Group, Int) -> Boolean): Tracks.Group? {
    if (length == 1 && filter(this, 0)) return null
    val listIndexToKeep = ArrayList<Int>(length)
    for (i in 0 until length) {
        if (!filter(this, i)) {
            listIndexToKeep.add(i)
        }
    }
    if (listIndexToKeep.isEmpty()) return null
    if (listIndexToKeep.size == length) return this
    val count = listIndexToKeep.size
    val formats = ArrayList<Format>(count)
    val trackSupport = IntArray(count)
    val trackSelect = BooleanArray(count)
    for (i in 0 until count) {
        val trackIndex = listIndexToKeep[i]
        formats.add(getTrackFormat(trackIndex))
        trackSupport[i] = getTrackSupport(trackIndex)
        trackSelect[i] = isTrackSelected(trackIndex)
    }
    return Tracks.Group(TrackGroup(mediaTrackGroup.id, *formats.toTypedArray()), isAdaptiveSupported, trackSupport, trackSelect)
}
