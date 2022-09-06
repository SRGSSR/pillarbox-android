/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.data

import com.google.gson.annotations.SerializedName

/**
 * Media composition
 *
 * @property chapterUrn urn of the chapter we want to use.
 * @property listChapter have to contain one chapter with urn = [chapterUrn]
 */
data class MediaComposition(
    val chapterUrn: String,
    @SerializedName("chapterList") val listChapter: List<Chapter>
) {
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
