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
 * @return The current media item chapters or an empty list.
 */
fun Player.getCurrentChapters(): List<Chapter> {
    return currentMediaItem?.mediaMetadata?.chapters ?: emptyList()
}

/**
 * @return The current media item credits or an empty list.
 */
fun Player.getCurrentCredits(): List<Credit> {
    return currentMediaItem?.mediaMetadata?.credits.orEmpty()
}

/**
 * Get the chapter at [position][positionMs].
 *
 * @param positionMs The position, in milliseconds, to find the chapter from.
 * @return `null` if there is no chapter at [positionMs].
 */
fun Player.getChapterAtPosition(positionMs: Long = currentPosition): Chapter? {
    return getCurrentChapters().firstOrNullAtPosition(positionMs)
}

/**
 * Get the credit at [position][positionMs].
 *
 * @param positionMs The position, in milliseconds, to find the credit from.
 * @return `null` if there is no credit at [positionMs].
 */
fun Player.getCreditAtPosition(positionMs: Long = currentPosition): Credit? {
    return getCurrentCredits().firstOrNullAtPosition(positionMs)
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
