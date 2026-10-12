/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.cast

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

    /**
     * Writes the chapters of this [PillarboxMetadata] into [customData], replacing any existing chapters.
     * If there are no chapters, [customData] is left unchanged.
     */
    fun PillarboxMetadata.appendToCustomData(customData: JSONObject) {
        if (chapters.isNotEmpty()) {
            val castChapters = chapters.map(CastChapter::fromChapter)
            customData.put(KEY_CHAPTERS, JSONArray(jsonSerializer.encodeToString(castChapters)))
        }
    }

    /**
     * Decode [List<CastChapter>?] from [JSONObject].
     */
    internal fun decodeCastChapters(customData: JSONObject): List<CastChapter>? {
        val chaptersJson = customData.optJSONArray(KEY_CHAPTERS) ?: return null
        return runCatching {
            jsonSerializer.decodeFromString<List<CastChapter>>(chaptersJson.toString())
        }.getOrNull()
    }
}
