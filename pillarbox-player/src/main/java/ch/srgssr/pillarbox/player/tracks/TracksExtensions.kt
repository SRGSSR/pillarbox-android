/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.extension.isForced

val Tracks.tracks: List<Track>
    get() = toTrackSequence()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .filterIsNotForced()
        .toList()

val Tracks.audioTracks: List<AudioTrack>
    get() = toTrackSequence()
        .filterIsInstance<AudioTrack>()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .toList()

val Tracks.textTracks: List<TextTrack>
    get() = toTrackSequence()
        .filterIsInstance<TextTrack>()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .filterIsNotForced()
        .toList()

val Tracks.videoTracks: List<VideoTrack>
    get() = toTrackSequence()
        .filterIsInstance<VideoTrack>()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .toList()

private fun Tracks.toTrackSequence(): Sequence<Track> {
    return groups
        .asSequence()
        .flatMapIndexed { groupIndex, group ->
            (0 until group.length).map { trackIndex ->
                Track(
                    group = group,
                    groupIndex = groupIndex,
                    trackIndexInGroup = trackIndex,
                )
            }
        }
}

private fun <T : Track> Sequence<T>.filterDuplicatedFormats(): Sequence<T> {
    return distinctBy { it.format.buildUpon().setId(0).build() }
}

private fun <T : Track> Sequence<T>.filterIsSupported(): Sequence<T> {
    return filter { it.isSupported }
}

private fun <T : Track> Sequence<T>.filterIsNotForced(): Sequence<T> {
    return filter { !it.format.isForced() }
}
