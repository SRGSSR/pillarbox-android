/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Page event
 *
 * @property title The page event title.
 * @property levels The page event levels.
 */
data class PageView(
    val title: String,
    val levels: List<String> = emptyList()
) {
    init {
        require(title.isNotBlank()) { "Title can't be blank!" }
    }
}
