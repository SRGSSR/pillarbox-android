/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * ComScore page view
 *
 * @property title The title of the page view.
 * @property customLabels The custom labels to send with this page view event.
 */
data class ComScorePageView(
    val title: String,
    val customLabels: Map<String, String> = emptyMap()
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank!" }
    }

    /**
     * Labels as a Map usable by ComScore.
     */
    fun toLabels(): Map<String, String> {
        val labels = HashMap<String, String>()
        labels.putAll(customLabels.filterValues { value -> value.isNotBlank() })
        labels[ComScoreLabel.C8.label] = title
        return labels
    }
}
