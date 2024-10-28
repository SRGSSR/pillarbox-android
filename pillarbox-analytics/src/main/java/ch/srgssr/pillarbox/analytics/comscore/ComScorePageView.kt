/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * Represents a page view event for ComScore.
 *
 * This class encapsulates the data required to track a page view event, including the page name and custom labels.
 *
 * @property name The name of the page being viewed. This property cannot be blank.
 * @property labels A map of custom labels to be associated with the page view event. Blank values are ignored and not sent. Defaults to an empty map.
 *
 * @throws IllegalArgumentException If [name] is blank.
 */
data class ComScorePageView(
    val name: String,
    val labels: Map<String, String> = emptyMap()
) {
    init {
        require(name.isNotBlank()) { "Title cannot be blank!" }
    }

    /**
     * Converts this object's properties into a [Map] of labels suitable for ComScore.
     *
     * @return A [Map] containing the labels, ready to be used by ComScore.
     */
    fun toLabels(): Map<String, String> {
        val labels = HashMap<String, String>()
        labels.putAll(this.labels.filterValues { value -> value.isNotBlank() })
        labels[ComScoreLabel.C8.label] = name
        return labels
    }
}
