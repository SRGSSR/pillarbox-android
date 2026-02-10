/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.standard.PlayerData
import ch.srgssr.pillarbox.standard.PlayerDataLoader
import kotlin.time.Duration.Companion.seconds

class PillarboxDemoService : PlayerDataLoader<CustomData> {

    override fun canLoad(mediaItem: MediaItem): Boolean {
        return mediaItem.mediaId.startsWith("pillarbox:")
    }

    override suspend fun load(mediaItem: MediaItem): PlayerData<CustomData> {
        val playerData = checkNotNull(datas[mediaItem.mediaId])
        if (!playerData.customData?.blockingReason.isNullOrBlank()) {
            throw BlockReasonException.Unknown()
        }
        return playerData
    }

    companion object {
        val datas: Map<String, PlayerData<CustomData>> = mapOf(
            "pillarbox:no_source" to PlayerData(),
            "pillarbox:video:1" to PlayerData(
                identifier = "pillarbox:video:1",
                source = PlayerData.Source(url = SamplesApple.Advanced_16_9_HEVC_h264.uri, type = PlayerData.Source.Type.VIDEO)
            ),
            "pillarbox:video:2" to PlayerData(
                identifier = "pillarbox:video:2",
                source = PlayerData.Source(url = SamplesApple.Advanced_16_9_HEVC_h264.uri, type = PlayerData.Source.Type.VIDEO),
                timeRanges = listOf(
                    PlayerData.TimeRange(startTime = 0L, endTime = 10.seconds.inWholeMilliseconds, type = "blocked"),
                    PlayerData.TimeRange(startTime = 20.seconds.inWholeMilliseconds, endTime = 50.seconds.inWholeMilliseconds, type = "blocked"),
                )
            ),
            "pillarbox:video:blocked" to PlayerData(
                identifier = "pillarbox:video:blocked",
                customData = CustomData("A reason to block")
            )
        )
    }
}
