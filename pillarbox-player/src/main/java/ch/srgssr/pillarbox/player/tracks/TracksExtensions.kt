/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import androidx.media3.common.C
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.extension.hasRole
import ch.srgssr.pillarbox.player.extension.isForced

/**
 * All the supported tracks for the currently played [MediaItem][androidx.media3.common.MediaItem].
 */
val Tracks.tracks: List<Track>
    get() = toTrackSequence()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .filterIsNotForced()
        .filterIsNotTrickPlay()
        .toList()

/**
 * All the supported audio tracks for the currently played [MediaItem][androidx.media3.common.MediaItem].
 */
val Tracks.audioTracks: List<AudioTrack>
    get() = toTrackSequence()
        .filterIsInstance<AudioTrack>()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .toList()

/**
 * All the supported text tracks for the currently played [MediaItem][androidx.media3.common.MediaItem].
 */
val Tracks.textTracks: List<TextTrack>
    get() = toTrackSequence()
        .filterIsInstance<TextTrack>()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .filterIsNotForced()
        .toList()

/**
 * All the supported video tracks for the currently played [MediaItem][androidx.media3.common.MediaItem].
 */
val Tracks.videoTracks: List<VideoTrack>
    get() = toTrackSequence()
        .filterIsInstance<VideoTrack>()
        .filterDuplicatedFormats()
        .filterIsSupported()
        .filterIsNotTrickPlay()
        .toList()

private fun Tracks.toTrackSequence(): Sequence<Track> {
    return groups
        .asSequence()
        .flatMapIndexed { groupIndex, group ->
            (0 until group.length).mapNotNull { trackIndex ->
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
    return filter { it.group.isTrackSupported(it.trackIndexInGroup) }
}

private fun <T : Track> Sequence<T>.filterIsNotForced(): Sequence<T> {
    return filter { !it.format.isForced() }
}

private fun <T : Track> Sequence<T>.filterIsNotTrickPlay(): Sequence<T> {
    return filter { !it.format.hasRole(C.ROLE_FLAG_TRICK_PLAY) }
}
