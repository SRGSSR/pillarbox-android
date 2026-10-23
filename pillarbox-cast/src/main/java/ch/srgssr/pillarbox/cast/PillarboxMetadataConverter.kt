/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.cast

import ch.srgssr.pillarbox.cast.data.CastBlockedSegment
import ch.srgssr.pillarbox.cast.data.CastChapter
import ch.srgssr.pillarbox.cast.data.CastTimeRange
import ch.srgssr.pillarbox.cast.data.PillarboxCastCustomData
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.network.jsonSerializer
import org.json.JSONArray
import org.json.JSONObject

/**
 * Adapter for Cast that converts [PillarboxMetadata] to and from a Cast `customData` [JSONObject].
 *
 * Chapters, blocked segments and time ranges are stored under the `chapters`, `blockedSegments` and `timeRanges` keys, using the same
 * format as the SRG SSR web receiver.
 */
object PillarboxMetadataConverter {
    internal const val KEY_CHAPTERS = "chapters"
    internal const val KEY_BLOCKED_SEGMENTS = "blockedSegments"
    internal const val KEY_TIME_RANGES = "timeRanges"

    /**
     * Writes the chapters, blocked time ranges and credits of this [PillarboxMetadata] into [customData], replacing any existing values.
     * An empty list removes the corresponding key. Other keys of [customData] are left unchanged.
     */
    fun PillarboxMetadata.appendToCustomData(customData: JSONObject) {
        customData.putList(KEY_CHAPTERS, chapters.map(CastChapter::fromChapter))
        customData.putList(KEY_BLOCKED_SEGMENTS, blockedTimeRanges.map(CastBlockedSegment::fromBlockedTimeRange))
        customData.putList(KEY_TIME_RANGES, credits.map(CastTimeRange::fromCredit))
    }

    /**
     * Decode [PillarboxCastCustomData] from [JSONObject].
     *
     * Each key is decoded independently, so an invalid value only discards that key.
     */
    internal fun decodeCustomData(customData: JSONObject): PillarboxCastCustomData {
        return PillarboxCastCustomData(
            chapters = customData.decodeList(KEY_CHAPTERS),
            blockedSegments = customData.decodeList(KEY_BLOCKED_SEGMENTS),
            timeRanges = customData.decodeList(KEY_TIME_RANGES),
        )
    }

    private inline fun <reified T> JSONObject.putList(key: String, values: List<T>) {
        if (values.isEmpty()) {
            remove(key)
        } else {
            put(key, JSONArray(jsonSerializer.encodeToString(values)))
        }
    }

    private inline fun <reified T> JSONObject.decodeList(key: String): List<T> {
        val json = optJSONArray(key) ?: return emptyList()
        return runCatching {
            jsonSerializer.decodeFromString<List<T>>(json.toString())
        }.getOrDefault(emptyList())
    }
}
