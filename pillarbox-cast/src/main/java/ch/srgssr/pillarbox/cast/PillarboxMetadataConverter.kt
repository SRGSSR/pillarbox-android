/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.cast

import ch.srgssr.pillarbox.cast.data.CastBlockedSegment
import ch.srgssr.pillarbox.cast.data.CastTimeRange
import ch.srgssr.pillarbox.cast.data.PillarboxCastCustomData
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.network.jsonSerializer
import org.json.JSONArray
import org.json.JSONObject

/**
 * Adapter for Cast that converts [PillarboxMetadata] to and from a Cast `customData` [JSONObject].
 *
 * Chapters are stored under the `chapters` key, using the same format as the SRG SSR web receiver.
 */
object PillarboxMetadataConverter {
    internal const val KEY_CHAPTERS = "chapters"
    internal const val KEY_BLOCKED_SEGMENT = "blockedSegments"
    internal const val KEY_TIME_RANGES = "timeRanges"

    /**
     * Writes the chapters of this [PillarboxMetadata] into [customData], replacing any existing chapters.
     * If there are no chapters, [customData] is left unchanged.
     */
    fun PillarboxMetadata.appendToCustomData(customData: JSONObject) {
        if (chapters.isNotEmpty()) {
            val castChapters = chapters.map(CastChapter::fromChapter)
            customData.put(KEY_CHAPTERS, JSONArray(jsonSerializer.encodeToString(castChapters)))
        }
        if (blockedTimeRanges.isNotEmpty()) {
            val castBlockedTimeRanges = blockedTimeRanges.map(CastBlockedSegment::fromBlockedTimeRange)
            customData.put(KEY_BLOCKED_SEGMENT, JSONArray(jsonSerializer.encodeToString(castBlockedTimeRanges)))
        }
        if (credits.isNotEmpty()) {
            val credits = credits.map(CastTimeRange::fromCredit)
            customData.put(KEY_TIME_RANGES, JSONArray(jsonSerializer.encodeToString(credits)))
        }
    }

    /**
     * Decode [PillarboxCastCustomData] from [JSONObject].
     */
    internal fun decodeCustomData(customData: JSONObject): PillarboxCastCustomData? {
        return runCatching {
            jsonSerializer.decodeFromString<PillarboxCastCustomData>(customData.toString())
        }.getOrNull()
    }
}
