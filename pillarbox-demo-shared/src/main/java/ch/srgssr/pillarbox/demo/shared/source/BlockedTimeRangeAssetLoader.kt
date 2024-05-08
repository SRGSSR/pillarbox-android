/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * An AssetLoader to demonstrate some edge cases with [BlockedTimeRange].
 */
class BlockedTimeRangeAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration?.uri?.toString()?.startsWith("blocked:") ?: false
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val mediaId = mediaItem.mediaId
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(URL)),
            blockedTimeRanges = createBlockedTimeRangesFromId(mediaId)
        )
    }

    @Suppress("MagicNumber")
    private fun createBlockedTimeRangesFromId(mediaId: String): List<BlockedTimeRange> {
        return when (mediaId) {
            ID_START_END -> {
                listOf(
                    BlockedTimeRange(0, 10_000L),
                    BlockedTimeRange(start = (videoDuration - 5.minutes).inWholeMilliseconds, end = videoDuration.inWholeMilliseconds)
                )
            }

            ID_OVERLAP -> {
                listOf(
                    BlockedTimeRange(10_000L, 50_000L),
                    BlockedTimeRange(15_000L, 5.minutes.inWholeMilliseconds)
                )
            }

            ID_INCLUDED -> {
                listOf(
                    BlockedTimeRange(15_000L, 30_000L, reason = "contained"),
                    BlockedTimeRange(10_000L, 60_000L, reason = "big"),
                )
            }

            else -> emptyList()
        }
    }

    companion object {
        private val URL = DemoItem.AppleBasic_16_9_TS_HLS.uri
        private val videoDuration = 1800.05.seconds

        private const val ID_START_END = "blocked://StartEnd"
        private const val ID_OVERLAP = "blocked://Overlap"
        private const val ID_INCLUDED = "blocked://Included"

        /**
         * DemoItem to test BlockedTimeRange at start and end of the media.
         */
        val DemoItemBlockedTimeRangeAtStartAndEnd = DemoItem(
            title = "Start and ends with a blocked time range",
            uri = ID_START_END,
            description = "Blocked times ranges at 00:00"
        )
        /**
         * DemoItem to test overlapping BlockedTimeRange.
         */
        val DemoItemBlockedTimeRangeOverlaps = DemoItem(
            title = "Blocked time range are overlapping",
            uri = ID_OVERLAP,
            description = "Two blocked time range are overlapping at 10s"
        )

        /**
         * DemoItem to test included BlockedTimeRange
         */
        val DemoItemBlockedTimeRangeIncluded = DemoItem(
            title = "Blocked time range is included",
            uri = ID_INCLUDED,
            description = "One blocked time range is included inside the other"
        )
    }
}
