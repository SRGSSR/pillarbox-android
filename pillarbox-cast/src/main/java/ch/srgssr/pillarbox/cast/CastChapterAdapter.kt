/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.cast

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import org.json.JSONArray
import org.json.JSONObject

/**
 * Adapter for Cast that converts [Chapter] to JSON and back.
 */
object CastChapterAdapter {
    private const val CHAPTER_TITLE = "title"
    private const val CHAPTER_IMAGE_URL = "imageUrl"
    private const val CHAPTER_ID = "id"
    private const val CHAPTER_START = "start"
    private const val CHAPTER_END = "end"
    private const val CHAPTERS = "chapters"

    /**
     * Converts a [JSONObject] to a list of [Chapter].
     */
    fun fromJson(chapters: JSONObject?): List<Chapter>? {
        val chaptersArray = chapters?.getJSONArray(CHAPTERS) ?: return null
        val chapterList = mutableListOf<Chapter>()
        for (i in 0 until chaptersArray.length()) {
            val chapterJson = chaptersArray.getJSONObject(i)
            val chapter = Chapter(
                id = chapterJson.getString(CHAPTER_ID),
                start = chapterJson.getLong(CHAPTER_START),
                end = chapterJson.getLong(CHAPTER_END),
                mediaMetadata = MediaMetadata.Builder()
                    .apply {
                        if (chapterJson.has(CHAPTER_TITLE)) {
                            setTitle(chapterJson.getString(CHAPTER_TITLE))
                        }
                        if (chapterJson.has(CHAPTER_IMAGE_URL)) {
                            setArtworkUri(chapterJson.getString(CHAPTER_IMAGE_URL).toUri())
                        }
                    }
                    .build()
            )
            chapterList.add(chapter)
        }
        return chapterList
    }

    /**
     * Converts a list of [Chapter] to a [JSONObject].
     */
    fun toJson(chapters: List<Chapter>): JSONObject {
        val jsons = chapters.map { chapter ->
            JSONObject(
                mapOf<String, Any>(
                    CHAPTER_ID to chapter.id,
                    CHAPTER_START to chapter.start,
                    CHAPTER_END to chapter.end,
                )
            ).apply {
                chapter.mediaMetadata.title?.let {
                    put(CHAPTER_TITLE, it.toString())
                }
                chapter.mediaMetadata.artworkUri?.let {
                    put(CHAPTER_IMAGE_URL, it.toString())
                }
            }
        }
        val jsonArray = JSONArray(jsons)

        return JSONObject().put(CHAPTERS, jsonArray)
    }
}
