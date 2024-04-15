/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.asset.BlockedInterval
import ch.srgssr.pillarbox.player.asset.Chapter
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.dash.manifest.DashManifest
import androidx.media3.exoplayer.hls.HlsManifest
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds

/**
 * Get a snapshot of the current media items
 */
fun Player.getCurrentMediaItems(): List<MediaItem> {
    val count = mediaItemCount
    if (count == 0) {
        return emptyList()
    }
    return buildList(count) {
        repeat(count) { i ->
            add(getMediaItemAt(i))
        }
    }
}

/**
 * Get playback speed
 *
 * @return [Player.getPlaybackParameters] speed
 */
fun Player.getPlaybackSpeed(): Float {
    return playbackParameters.speed
}

/**
 * Current position percent
 *
 * @return the current position in percent [0,1].
 */
fun Player.currentPositionPercentage(): Float {
    return currentPosition / duration.coerceAtLeast(1).toFloat()
}

/**
 * Handle audio focus with the currently set [AudioAttributes][androidx.media3.common.AudioAttributes].
 * @param handleAudioFocus `true` if the player should handle audio focus, `false` otherwise.
 */
fun Player.setHandleAudioFocus(handleAudioFocus: Boolean) {
    setAudioAttributes(audioAttributes, handleAudioFocus)
}

/**
 * Get current chapters
 *
 * @return current media item chapters.
 */
fun Player.getCurrentChapters(): List<Chapter> {
    return currentMediaItem?.pillarboxData?.chapters ?: emptyList()
}

/**
 * Get current chapter
 *
 * @param positionMs the position in millisecond to find the chapter from.
 * @return null if there is no chapter at [positionMs].
 */
fun Player.getChapterAtPosition(positionMs: Long = currentPosition): Chapter? {
    if (positionMs == C.TIME_UNSET) return null
    return getCurrentChapters().firstOrNull { positionMs in it }
}

/**
 * Is at live edge
 *
 * @param positionMs The position in milliseconds.
 * @param window The optional Window.
 * @return if [positionMs] is at live edge.
 */
fun Player.isAtLiveEdge(positionMs: Long = currentPosition, window: Window = Window()): Boolean {
    if (!isCurrentMediaItemLive) return false
    currentTimeline.getWindow(currentMediaItemIndex, window)
    val offsetSeconds = when (val manifest = currentManifest) {
        is HlsManifest -> {
            manifest.mediaPlaylist.targetDurationUs.microseconds.inWholeSeconds
        }

        is DashManifest -> {
            manifest.minBufferTimeMs.milliseconds.inWholeSeconds
        }

        else -> {
            0L
        }
    }
    return playWhenReady && positionMs.milliseconds.inWholeSeconds >= window.defaultPositionMs.milliseconds.inWholeSeconds - offsetSeconds
}

fun Player.getCurrentBlockedIntervals(): List<BlockedInterval> {
    return currentMediaItem?.pillarboxData?.blockedIntervals ?: emptyList()
}

fun Player.getBlockedIntervalAtPosition(positionMs: Long): BlockedInterval? {
    if (positionMs == C.TIME_UNSET) return null
    return getCurrentBlockedIntervals().firstOrNull { positionMs in it }
}
