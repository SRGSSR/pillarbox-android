/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * An AssetLoader to demonstrate some edge cases with [BlockedTimeRange].
 */
class BlockedTimeRangeAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration?.uri?.toString()?.startsWith("blocked:") == true
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val id = mediaItem.localConfiguration?.uri?.toString().orEmpty()
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(URL)),
            pillarboxMetadata = PillarboxMetadata(
                credits = createCreditsFromId(id),
                blockedTimeRanges = createBlockedTimeRangesFromId(id),
            ),
        )
    }

    @Suppress("MagicNumber")
    private fun createBlockedTimeRangesFromId(id: String): List<BlockedTimeRange> {
        return when (id) {
            ID_START_END -> {
                listOf(
                    BlockedTimeRange(
                        start = 0L,
                        end = 10.seconds.inWholeMilliseconds,
                    ),
                    BlockedTimeRange(
                        start = (videoDuration - 5.minutes).inWholeMilliseconds,
                        end = videoDuration.inWholeMilliseconds,
                    )
                )
            }

            ID_OVERLAP -> {
                listOf(
                    BlockedTimeRange(
                        start = 10.seconds.inWholeMilliseconds,
                        end = 50.seconds.inWholeMilliseconds,
                    ),
                    BlockedTimeRange(
                        start = 15.seconds.inWholeMilliseconds,
                        end = 5.minutes.inWholeMilliseconds,
                    )
                )
            }

            ID_INCLUDED -> {
                listOf(
                    BlockedTimeRange(
                        start = 15.seconds.inWholeMilliseconds,
                        end = 30.seconds.inWholeMilliseconds,
                        reason = "contained",
                    ),
                    BlockedTimeRange(
                        start = 10.seconds.inWholeMilliseconds,
                        end = 1.minutes.inWholeMilliseconds,
                        reason = "big",
                    ),
                )
            }

            ID_WITH_CREDITS -> {
                listOf(
                    BlockedTimeRange(
                        start = 5.minutes.inWholeMilliseconds,
                        end = 6.minutes.inWholeMilliseconds,
                    ),
                    BlockedTimeRange(
                        start = 15.minutes.inWholeMilliseconds,
                        end = 16.5.minutes.inWholeMilliseconds,
                    ),
                )
            }

            else -> emptyList()
        }
    }

    @Suppress("MagicNumber")
    private fun createCreditsFromId(id: String): List<Credit> {
        return when (id) {
            ID_WITH_CREDITS -> {
                listOf(
                    Credit.Opening(
                        start = 0L,
                        end = 1.5.minutes.inWholeMilliseconds,
                    ),
                    Credit.Closing(
                        start = 28.minutes.inWholeMilliseconds,
                        end = videoDuration.inWholeMilliseconds,
                    ),
                )
            }

            else -> emptyList()
        }
    }

    @Suppress("StringLiteralDuplication")
    companion object {
        private val URL = SamplesApple.Basic_16_9.uri

        private val videoDuration = 1800.05.seconds

        private const val ID_START_END = "blocked://StartEnd"
        private const val ID_OVERLAP = "blocked://Overlap"
        private const val ID_INCLUDED = "blocked://Included"
        private const val ID_WITH_CREDITS = "blocked://WithCredits"

        /**
         * [DemoItem] to test [BlockedTimeRange] at start and end of the media.
         */
        val DemoItemBlockedTimeRangeAtStartAndEnd = DemoItem.URL(
            title = "Starts and ends with a blocked time range",
            uri = ID_START_END,
            description = "Blocked times ranges at 00:00 - 00:10 and 25:00 - 30:00",
            languageTag = "en-CH",
        )

        /**
         * [DemoItem] to test overlapping [BlockedTimeRange].
         */
        val DemoItemBlockedTimeRangeOverlaps = DemoItem.URL(
            title = "Blocked time ranges are overlapping",
            uri = ID_OVERLAP,
            description = "Blocked times ranges at 00:10 to 00:50 and 00:15 to 05:00",
            languageTag = "en-CH",
        )

        /**
         * [DemoItem] to test included [BlockedTimeRange].
         */
        val DemoItemBlockedTimeRangeIncluded = DemoItem.URL(
            title = "Blocked time range is included in an other one",
            uri = ID_INCLUDED,
            description = "Blocked times ranges at 00:15 - 00:30 and 00:10 - 01:00",
            languageTag = "en-CH",
        )

        /**
         * [DemoItem] to test [BlockedTimeRange] alongside opening and closing [Credit].
         */
        val DemoItemBlockedTimeRangeWithCredits = DemoItem.URL(
            title = "Blocked time ranges and credits",
            uri = ID_WITH_CREDITS,
            description = "Blocked times ranges at 05:00 - 06:00 and 15:00 - 16:30, credits at 00:00 - 01:30 and 28:00 - 30:00",
            languageTag = "en-CH",
        )
    }
}
