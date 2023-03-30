/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Base event
 */
sealed interface BaseEvent {
    /**
     * Custom labels to inject to Analytics providers
     */
    val customLabels: CustomLabels?
}
