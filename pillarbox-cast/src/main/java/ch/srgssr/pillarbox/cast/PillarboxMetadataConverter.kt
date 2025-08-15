/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.cast

import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.jsonTimeRanges
import org.json.JSONObject

/**
 * Adapter for Cast that converts [PillarboxMetadata] to and from JSONObject.
 */
object PillarboxMetadataConverter {
    internal const val KEY_PILLARBOX = "pillarbox"

    /**
     * Extension function to add [PillarboxMetadata] to [JSONObject].
     */
    fun PillarboxMetadata.appendToCustomData(customData: JSONObject) = runCatching {
        val jsonObj = jsonTimeRanges.encodeToString(this)
        customData.put(KEY_PILLARBOX, JSONObject(jsonObj))
    }

    /**
     * Decode [PillarboxMetadata] from [JSONObject].
     */
    fun decodePillarboxMetadata(customData: JSONObject): PillarboxMetadata? {
        return runCatching {
            val json = customData.getJSONObject(KEY_PILLARBOX)
            jsonTimeRanges.decodeFromString<PillarboxMetadata>(json.toString())
        }.getOrNull()
    }
}
