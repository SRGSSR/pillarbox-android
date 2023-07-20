/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * ComScore page view
 *
 * @property name The name of the page view.
 * @property labels The custom labels to send with this page view event. Blank values are not send.
 */
data class ComScorePageView(
    val name: String,
    val labels: Map<String, String> = emptyMap()
) {
    init {
        require(name.isNotBlank()) { "Title cannot be blank!" }
    }

    /**
     * Labels as a Map usable by ComScore.
     */
    fun toLabels(): Map<String, String> {
        val labels = HashMap<String, String>()
        labels.putAll(this.labels.filterValues { value -> value.isNotBlank() })
        labels[ComScoreLabel.C8.label] = name
        return labels
    }
}
