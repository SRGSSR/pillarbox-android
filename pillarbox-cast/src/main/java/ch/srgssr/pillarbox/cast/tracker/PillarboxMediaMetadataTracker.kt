/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.tracker

import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.TimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition
import ch.srgssr.pillarbox.player.extension.chapters
import ch.srgssr.pillarbox.player.extension.credits

internal class PillarboxMediaMetadataTracker(private val player: PillarboxCastPlayer) : Player.Listener {
    init {
        player.addListener(this)
    }

    private var currentChapter: Chapter? = null
        set(value) {
            if (value != field) {
                player.notifyChapterChanged(value)
                field = value
            }
        }
    private var currentCredit: Credit? = null
        set(value) {
            if (value != field) {
                player.notifyCreditChanged(value)
                field = value
            }
        }
    private var chapterList: List<Chapter> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                currentChapter = getTimeRangeAt(value, currentChapter, player.currentPosition)
            }
        }
    private var creditList: List<Credit> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                currentCredit = getTimeRangeAt(value, currentCredit, player.currentPosition)
            }
        }

    fun updateWithPosition(position: Long) {
        currentChapter = getTimeRangeAt(chapterList, currentChapter, position)
        currentCredit = getTimeRangeAt(creditList, currentCredit, position)
    }

    private fun <T : TimeRange> getTimeRangeAt(listTimeRanges: List<T>, currentTimeRange: T?, position: Long): T? {
        return currentTimeRange
            ?.takeIf { timeRange -> position in timeRange }
            ?: listTimeRanges.firstOrNullAtPosition(position)
    }

    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
        when (reason) {
            Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> {
                if (oldPosition.mediaItemIndex == newPosition.mediaItemIndex) {
                    val position = newPosition.positionMs
                    updateWithPosition(position)
                } else {
                    clear()
                }
            }

            else -> {
                clear()
            }
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        chapterList = mediaMetadata.chapters ?: emptyList()
        creditList = mediaMetadata.credits ?: emptyList()
    }

    fun clear() {
        currentChapter = null
        currentCredit = null
        chapterList = emptyList()
        creditList = emptyList()
    }
}
