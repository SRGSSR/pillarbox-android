/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.cast

import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.jsonTimeRanges
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Adapter for Cast that converts [Chapter] to JSON and back.
 */
object PillarboxMetadataConverter {
    private const val CHAPTERS = "chapters"
    private const val CREDITS = "credits"

    /**
     * Try to decode the field [CHAPTERS] from the given [JSONObject].
     */
    fun decodeChapters(customData: JSONObject): List<Chapter>? {
        return runCatching {
            val jsonArray = customData.getJSONArray(CHAPTERS)
            jsonTimeRanges.decodeFromString<List<Chapter>>(jsonArray.toString())
        }.getOrNull()
    }

    /**
     * Append a [JSONArray] to the given [JSONObject] with the field [CHAPTERS].
     */
    fun appendChapters(customData: JSONObject, listChapter: List<Chapter>) {
        if (listChapter.isNotEmpty()) {
            customData.put(CHAPTERS, JSONArray(JSONTokener(jsonTimeRanges.encodeToString(listChapter))))
        }
    }

    /**
     * Try to decode the field [CREDITS] from the given [JSONObject].
     */
    fun decodeCredits(customData: JSONObject): List<Credit>? {
        return runCatching {
            val jsonArray = customData.getJSONArray(CREDITS)
            jsonTimeRanges.decodeFromString<List<Credit>>(jsonArray.toString())
        }.getOrNull()
    }

    /**
     * Append a [JSONArray] to the given [JSONObject] with the field [CREDITS].
     */
    fun appendCredits(customData: JSONObject, listCredit: List<Credit>) {
        if (listCredit.isNotEmpty()) {
            customData.put(CREDITS, JSONArray(JSONTokener(jsonTimeRanges.encodeToString(listCredit))))
        }
    }
}
