/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Media composition
 *
 * @property chapterUrn urn of the chapter we want to use.
 * @property listChapter have to contain one chapter with urn = [chapterUrn]
 * @property comScoreAnalyticsLabels
 * @property analyticsLabels
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
     * Main chapter
     *
     * @return Chapter from [chapterUrn]
     */
    val mainChapter: Chapter
        get() = findChapterByUrn(chapterUrn)!!

    /**
     * Find chapter by urn inside [listChapter]
     *
     * To get the Chapter from [chapterUrn] mainChapter
     *
     * @param urn of the Chapter
     * @return null if not chapter found in [listChapter]
     */
    fun findChapterByUrn(urn: String): Chapter? {
        return listChapter.find { it.urn == urn }
    }
}
