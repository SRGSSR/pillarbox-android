/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.dash.manifest.DashManifest
import androidx.media3.exoplayer.hls.HlsManifest
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds

/**
 * Retrieves a snapshot of the current media items in the player.
 *
 * @return A list of [MediaItem], or an empty list if no items are set.
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
 * Returns the current playback speed of the player.
 *
 * @return The current playback speed as a float value.
 */
fun Player.getPlaybackSpeed(): Float {
    return playbackParameters.speed
}

/**
 * Returns the current playback position as a percentage of the total duration.
 *
 * @return The current playback position as a percentage, ranging from 0.0 to 1.0.
 */
fun Player.currentPositionPercentage(): Float {
    return currentPosition / duration.coerceAtLeast(1).toFloat()
}

/**
 * Sets whether the player should handle audio focus.
 *
 * @param handleAudioFocus `true` if the player should handle audio focus, `false` otherwise.
 */
fun Player.setHandleAudioFocus(handleAudioFocus: Boolean) {
    setAudioAttributes(audioAttributes, handleAudioFocus)
}

/**
 * Returns the chapters for the currently playing media item.
 *
 * @return A list of [Chapter] for the currently playing media item, or an empty list if there are no chapters or no current media item.
 */
fun Player.getCurrentChapters(): List<Chapter> {
    return currentMediaItem?.mediaMetadata?.chapters ?: emptyList()
}

/**
 * Returns the credits for the currently playing media item.
 *
 * @return A list of [Credit] for the currently playing media item, or an empty list if there are no credits or no current media item.
 */
fun Player.getCurrentCredits(): List<Credit> {
    return currentMediaItem?.mediaMetadata?.credits.orEmpty()
}

/**
 * Retrieves the [Chapter] that encompasses the given position in the media playback.
 *
 * @param positionMs The position in the media playback, in milliseconds.
 * @return The [Chapter] at the given position, or `null` if no chapter is found at that position.
 */
fun Player.getChapterAtPosition(positionMs: Long = currentPosition): Chapter? {
    return getCurrentChapters().firstOrNullAtPosition(positionMs)
}

/**
 * Retrieves the [Credit] that encompasses the given position in the media playback.
 *
 * @param positionMs The position in the media playback, in milliseconds.
 * @return The [Credit] at the given position, or `null` if no credit is found at that position.
 */
fun Player.getCreditAtPosition(positionMs: Long = currentPosition): Credit? {
    return getCurrentCredits().firstOrNullAtPosition(positionMs)
}

/**
 * Checks if the current playback position is at the live edge of a live stream.
 *
 * @param positionMs The playback position, in milliseconds, to check.
 * @param window A [Window] to store the current window information.
 * @return Whether the playback position is at the live edge.
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

/**
 * Get the player's position timestamp of the media being played, or `null` if not available.
 *
 * @param window A reusable [Window] instance.
 *
 * @return The player's position timestamp of the media being played, in milliseconds, or `null` if not available.
 */
internal fun Player.getPositionTimestamp(window: Window = Window()): Long? {
    if (currentTimeline.isEmpty) {
        return null
    }

    currentTimeline.getWindow(currentMediaItemIndex, window)

    return if (window.elapsedRealtimeEpochOffsetMs != C.TIME_UNSET) {
        window.windowStartTimeMs + currentPosition
    } else {
        null
    }
}
