/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.annotation.SuppressLint
import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * Text tracks.
 */
@Deprecated(
    message = "Use the `textTracks` extension instead",
    replaceWith = ReplaceWith(
        expression = "textTracks",
        imports = ["ch.srgssr.pillarbox.player.tracks.textTracks"],
    ),
)
val Tracks.text: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_TEXT).mapNotNull { it.filterForcedAndUnsupported() }

/**
 * Audio tracks.
 */
@Deprecated(
    message = "Use the `audioTracks` extension instead",
    replaceWith = ReplaceWith(
        expression = "audioTracks",
        imports = ["ch.srgssr.pillarbox.player.tracks.audioTracks"],
    ),
)
val Tracks.audio: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_AUDIO).mapNotNull { it.filterUnsupported() }

/**
 * Video tracks.
 */
@Deprecated(
    message = "Use the `videoTracks` extension instead",
    replaceWith = ReplaceWith(
        expression = "videoTracks",
        imports = ["ch.srgssr.pillarbox.player.tracks.videoTracks"],
    ),
)
val Tracks.video: List<Tracks.Group>
    get() = filterByTrackType(C.TRACK_TYPE_VIDEO).mapNotNull { it.filterUnsupported() }

private fun Tracks.filterByTrackType(trackType: @TrackType Int): List<Tracks.Group> {
    return groups.filter { it.type == trackType }
}

private fun Tracks.Group.filterForcedAndUnsupported(): Tracks.Group? {
    return filterBy { group, i -> group.isTrackSupported(i) && !group.getTrackFormat(i).isForced() }
}

internal fun Tracks.Group.filterUnsupported(): Tracks.Group? {
    return filterBy { group, i -> group.isTrackSupported(i) }
}

/**
 * Filter [Format] that matching [predicate].
 *
 * @param predicate function that takes the index of an element and the element itself and returns the result of predicate evaluation on the element.
 * @receiver
 * @return element matching [predicate] or null if filtered items is empty because [TrackGroup] can not be empty.
 */
@SuppressLint("WrongConstant")
@Suppress("SpreadOperator", "ReturnCount")
internal fun Tracks.Group.filterBy(predicate: (Tracks.Group, Int) -> Boolean): Tracks.Group? {
    val listIndexMatchingPredicate = ArrayList<Int>(length)
    for (i in 0 until length) {
        if (predicate(this, i)) {
            listIndexMatchingPredicate.add(i)
        }
    }
    // All format doesn't match predicate.
    if (listIndexMatchingPredicate.isEmpty()) return null
    // All format matching the predicate, nothing to change.
    if (listIndexMatchingPredicate.size == length) return this
    val count = listIndexMatchingPredicate.size
    val formats = ArrayList<Format>(count)
    val trackSupport = IntArray(count)
    val trackSelect = BooleanArray(count)
    for (i in 0 until count) {
        val trackIndex = listIndexMatchingPredicate[i]
        formats.add(getTrackFormat(trackIndex))
        trackSupport[i] = getTrackSupport(trackIndex)
        trackSelect[i] = isTrackSelected(trackIndex)
    }
    return Tracks.Group(TrackGroup(mediaTrackGroup.id, *formats.toTypedArray()), isAdaptiveSupported, trackSupport, trackSelect)
}

/**
 * @return [MediaItemTrackerData] if it exists, `null` otherwise
 */
fun Tracks.getMediaItemTrackerDataOrNull(): MediaItemTrackerData? {
    return groups.firstOrNull {
        it.type == PillarboxMediaSource.TRACK_TYPE_PILLARBOX_TRACKERS
    }?.getTrackFormat(0)?.customData as? MediaItemTrackerData
}

@Suppress("UNCHECKED_CAST")
internal fun Tracks.getBlockedTimeRangeOrNull(): List<BlockedTimeRange>? {
    return groups.firstOrNull {
        it.type == PillarboxMediaSource.TRACK_TYPE_PILLARBOX_BLOCKED
    }?.getTrackFormat(0)?.customData as? List<BlockedTimeRange>
}
