/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.tracker

import androidx.media3.common.Player
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

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

    fun updateWithPosition(position: Long) {
        currentChapter = player.getChapterAtPosition(position)
        currentCredit = player.getCreditAtPosition(position)
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

    fun clear() {
        currentChapter = null
        currentCredit = null
    }
}
