/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.cast

import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.TimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.jsonTimeRanges
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Adapter for Cast that converts [Chapter], [Credit] and [BlockedTimeRange] to JSON and back.
 */
object PillarboxMetadataConverter {
    private const val CHAPTERS = "chapters"
    private const val CREDITS = "credits"
    private const val BLOCKED_TIME_RANGES = "blockedTimeRanges"

    /**
     * Try to decode the field [CHAPTERS] from the given [JSONObject].
     */
    fun decodeChapters(customData: JSONObject): List<Chapter>? {
        return decodeTimesRangesFrom(customData, CHAPTERS)
    }

    /**
     * Append a [JSONArray] to the given [JSONObject] with the field [CHAPTERS].
     */
    fun appendChapters(customData: JSONObject, listChapter: List<Chapter>) {
        appendTimesRangesTo(customData, CHAPTERS, listChapter)
    }

    /**
     * Try to decode the field [CREDITS] from the given [JSONObject].
     */
    fun decodeCredits(customData: JSONObject): List<Credit>? {
        return decodeTimesRangesFrom(customData, CREDITS)
    }

    /**
     * Append a [JSONArray] to the given [JSONObject] with the field [CREDITS].
     */
    fun appendCredits(customData: JSONObject, listCredit: List<Credit>) {
        appendTimesRangesTo(customData, CREDITS, listCredit)
    }

    /**
     * Try to decode the field [BLOCKED_TIME_RANGES] from the given [JSONObject].
     */
    fun decodeBlockedTimeRanges(customData: JSONObject): List<BlockedTimeRange>? {
        return decodeTimesRangesFrom(customData, BLOCKED_TIME_RANGES)
    }

    /**
     * Append a [JSONArray] to the given [JSONObject] with the field [BLOCKED_TIME_RANGES].
     */
    fun appendBlockedTimeRanges(customData: JSONObject, listBlockedTimeRange: List<BlockedTimeRange>) {
        appendTimesRangesTo(customData, BLOCKED_TIME_RANGES, listBlockedTimeRange)
    }

    private inline fun <reified T : TimeRange> decodeTimesRangesFrom(customData: JSONObject, key: String): List<T>? {
        return runCatching {
            val jsonArray = customData.getJSONArray(key)
            jsonTimeRanges.decodeFromString<List<T>>(jsonArray.toString())
        }.getOrNull()
    }

    private inline fun <reified T : TimeRange> appendTimesRangesTo(customData: JSONObject, key: String, list: List<T>) {
        if (list.isNotEmpty()) {
            customData.put(key, JSONArray(JSONTokener(jsonTimeRanges.encodeToString(list))))
        }
    }
}
