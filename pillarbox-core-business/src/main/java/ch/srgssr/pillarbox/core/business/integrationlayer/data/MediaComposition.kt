/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a media composition.
 *
 * @property chapterUrn The URN of the main chapter within this composition.
 * @property listChapter A list of [Chapter]s, which must include the main chapter identified by [chapterUrn].
 * @property comScoreAnalyticsLabels Labels for ComScore analytics.
 * @property analyticsLabels Labels for Commanders Act analytics.
 */
@Serializable
data class MediaComposition(
    val chapterUrn: String,
    @SerialName("chapterList") val listChapter: List<Chapter>,
    @SerialName("analyticsData")
    override val comScoreAnalyticsLabels: Map<String, String>? = null,
    @SerialName("analyticsMetadata")
    override val analyticsLabels: Map<String, String>? = null,
) : DataWithAnalytics {
    /**
     * The main chapter, identified by [chapterUrn].
     *
     * @return The [Chapter] representing the main chapter.
     * @throws NullPointerException If no [Chapter] with the given URN is found.
     */
    val mainChapter: Chapter
        get() = findChapterByUrn(chapterUrn)!!

    /**
     * Finds a [Chapter] within the [list of chapters][listChapter] by its URN.
     *
     * @param urn The URN of the [Chapter] to search for.
     * @return The [Chapter] with the matching URN, or `null` if none is found.
     */
    fun findChapterByUrn(urn: String): Chapter? {
        return listChapter.find { it.urn == urn }
    }
}
