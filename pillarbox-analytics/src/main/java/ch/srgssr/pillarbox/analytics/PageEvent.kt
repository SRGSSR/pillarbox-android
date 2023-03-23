/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Page event
 *
 * @property title The page event title.
 * @property levels The page event levels
 * @property customLabels The page event custom labels.
 */
data class PageEvent(val title: String, val levels: Array<String>, val customLabels: Map<String, String>? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PageEvent

        if (title != other.title) return false
        if (customLabels != other.customLabels) return false
        if (!levels.contentEquals(other.levels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + customLabels.hashCode()
        result = 31 * result + levels.contentHashCode()
        return result
    }
}
