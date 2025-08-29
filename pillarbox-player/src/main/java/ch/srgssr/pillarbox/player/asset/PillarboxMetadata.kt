/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.os.Parcelable
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import kotlinx.parcelize.Parcelize

/**
 * @property chapters A list of [Chapter] instances representing chapters within the media.
 * @property credits  A list of [Credit] instances representing credits within the media.
 * @property blockedTimeRanges A list of [BlockedTimeRange] instances representing periods within the media that should be skipped during playback.
 */
@Parcelize
data class PillarboxMetadata(
    val chapters: List<Chapter> = emptyList(),
    val credits: List<Credit> = emptyList(),
    val blockedTimeRanges: List<BlockedTimeRange> = emptyList(),
) : Parcelable {

    companion object {

        /**
         * An empty [PillarboxMetadata] instance.
         */
        val EMPTY = PillarboxMetadata()
    }
}
